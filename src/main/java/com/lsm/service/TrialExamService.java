package com.lsm.service;

import com.lsm.model.DTOs.TrialExamRequestDTO;
import com.lsm.model.DTOs.TrialExamResponseDTO;
import com.lsm.model.entity.base.AppUser;
import com.lsm.repository.AppUserRepository;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.experimental.SuperBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.PostConstruct;

@Service
@Slf4j
@RequiredArgsConstructor
public class TrialExamService {

    private final AppUserRepository appUserRepository;

    @Value("${app.upload.dir}")
    private String uploadDir;
    private String examResultUploadDir;

    private static final Pattern ANSWER_PATTERN = Pattern.compile("[ABCDE ]+");
    private static final Pattern NAME_PATTERN = Pattern.compile("[A-ZĞÜŞİÖÇIa-zğüşıöç]+\\s+(?:[A-ZĞÜŞİÖÇIa-zğüşıöç]+\\s*)+");
    private static final Pattern TC_PATTERN = Pattern.compile("^(?!0)\\d{10}[02468]$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^05\\d{9}$");

    @PostConstruct
    private void init() {
        this.examResultUploadDir = createExamResultDir();
    }

    @Data
    @SuperBuilder
    public static class ExamResultBase {
        private int id;
        private String studentName;
        private String studentTC;
        private String studentPhoneNumber;
        private String examType;  // A, B, C, D
        private String classEntity;
    }

    @EqualsAndHashCode(callSuper = true)
    @Data
    @SuperBuilder
    public static class ExamResultTYT extends ExamResultBase {
        private List<String> turkceAnswers;     // 40 questions
        private List<String> sosyalAnswers;     // 20 questions
        private List<String> matematikAnswers;  // 40 questions
        private List<String> fenAnswers;        // 20 questions
    }

    @EqualsAndHashCode(callSuper = true)
    @Data
    @SuperBuilder
    public static class ExamResultAYT extends ExamResultBase {
        private List<String> turkceAnswers;     // 40 questions 24 + 10 + 6
        private List<String> sosyalAnswers;     // 40 questions 11 + 11 + 12 + 6
        private List<String> matematikAnswers;  // 40 questions
        private List<String> fenAnswers;        // 40 questions 14 + 13 + 13
    }

    @EqualsAndHashCode(callSuper = true)
    @Data
    @SuperBuilder
    public static class ExamResultYDT extends ExamResultBase {
        private List<String> answers;     // 80 questions
        private String language;
    }

    @EqualsAndHashCode(callSuper = true)
    @Data
    @SuperBuilder
    public static class ExamResultLGS extends ExamResultBase {
        private List<String> turkceAnswers;     // 20 questions
        private List<String> sosyalAnswers;     // 30 questions
        private List<String> matematikAnswers;  // 20 questions
        private List<String> fenAnswers;        // 20 questions
    }

    @Transactional
    public TrialExamResponseDTO evaluateTrialExam(AppUser loggedInUser, TrialExamRequestDTO requestDTO) {
        try {
            // Convert MultipartFile to String content
            String resultsContent = new String(requestDTO.getResults().getBytes(), StandardCharsets.UTF_8);
            String answerKeyContent = new String(requestDTO.getAnswerKey().getBytes(), StandardCharsets.UTF_8);

            String resultsCsv;
            String answerKeyCsv = switch (requestDTO.getExamType()) {
                case TYT -> {
                    resultsCsv = convertResultsToCsvTYT(resultsContent);
                    yield convertAnswerKeyToCsvTYT(answerKeyContent);
                }
                case AYT -> {
                    resultsCsv = convertResultsToCsvAYT(resultsContent);
                    yield convertAnswerKeyToCsvAYT(answerKeyContent);
                }
                case YDT -> {
                    resultsCsv = convertResultsToCsvYDT(resultsContent);
                    yield convertAnswerKeyToCsvYDT(answerKeyContent);
                }
                case LGS -> {
                    resultsCsv = convertResultsToCsvLGS(resultsContent);
                    yield convertAnswerKeyToCsvLGS(answerKeyContent);
                }
                default -> throw new IllegalArgumentException("Unsupported exam type: " + requestDTO.getExamType());
            };

            // Create temporary CSV files
            Path resultsPath = Files.createTempFile("results_", ".csv");
            Path answerKeyPath = Files.createTempFile("answer_key_", ".csv");

            // Write CSV content to temporary files
            Files.write(resultsPath, resultsCsv.getBytes());
            Files.write(answerKeyPath, answerKeyCsv.getBytes());

            // Generate PDF from evaluation results
            byte[] pdfContent = generatePdfReport(resultsPath, answerKeyPath);

            // TODO: save results as a past exam

            // Create unique filename for the PDF
            String pdfFileName = String.format("%s_%s_%s.pdf",
                    requestDTO.getExamType(),
                    requestDTO.getExamName().replaceAll("\\s+", "_"),
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")));

            // Save PDF to upload directory
            Path pdfPath = Paths.get(examResultUploadDir, pdfFileName);
            Files.write(pdfPath, pdfContent);

            // Clean up temporary files
            Files.deleteIfExists(resultsPath);
            Files.deleteIfExists(answerKeyPath);

            return TrialExamResponseDTO.builder()
                    .examName(requestDTO.getExamName())
                    .examDate(requestDTO.getExamDate())
                    .examType(requestDTO.getExamType())
                    .resultPdfUrl(pdfPath.toString())
                    .build();

        } catch (IOException e) {
            throw new RuntimeException("Error processing exam files: " + e.getMessage(), e);
        }
    }

    private byte[] generatePdfReport(Path resultsPath, Path answerKeyPath) throws IOException {
        // Implement PDF generation logic here
        // This should take the evaluation results and create a PDF report
        return null; // Replace with actual PDF generation
    }

    private String convertAnswerKeyToCsvTYT(String textContent) {
        StringBuilder csvContent = new StringBuilder();

        // Split content into lines
        String[] lines = textContent.split("\n");

        for (String line : lines) {
            // Remove any leading/trailing whitespace
            line = line.trim();

            if (!line.isEmpty()) {
                line = convertToTurkishChars(line);

                // Split line into parts (assuming space or tab separation in text file)
                String[] parts = line.split("\\s+");

                // Join parts with commas for CSV format
                String csvLine = String.join(",", parts);
                csvContent.append(csvLine).append("\n");
            }
        }

        return csvContent.toString();
    }

    private String convertResultsToCsvTYT(String textContent) {
        List<Integer> startColumnNameL   = new ArrayList<>(), endColumnNameL   = new ArrayList<>(),
                      startColumnTCL = new ArrayList<>(), endColumnTCL = new ArrayList<>(),
                      startColumnPhoneL    = new ArrayList<>(), endColumnPhoneL    = new ArrayList<>(),
                      startColumnTurkceL = new ArrayList<>(), endColumnTurkceL = new ArrayList<>(),
                      startColumnSosyalL = new ArrayList<>(), endColumnSosyalL = new ArrayList<>(),
                      startColumnMathL   = new ArrayList<>(), endColumnMathL   = new ArrayList<>(),
                      startColumnFenL    = new ArrayList<>(), endColumnFenL    = new ArrayList<>();

        BufferedReader reader = new BufferedReader(new StringReader(textContent));
        String line;

        try {
            while ((line = reader.readLine()) != null) {
                line = convertToTurkishChars(line);
                int startColumnName = 0, endColumnName = 0,
                        startColumnTurkce = 0, endColumnTurkce = 0,
                        startColumnSosyal = 0, endColumnSosyal = 0,
                        startColumnMath   = 0, endColumnMath   = 0,
                        startColumnFen    = 0, endColumnFen    = 0;
                for (int i = line.length() - 1; i >= 0; i--) {
                    char c = line.charAt(i);
                    if (c == 'A' || c == 'B' || c == 'C' || c == 'D' || c == 'E') {
                        endColumnFen = i;
                        startColumnFen = endColumnFen - 19; // 20 questions long
                        break;
                    }
                }
                for (int i = startColumnFen - 1; i >= 0; i--) {
                    char c = line.charAt(i);
                    if (c == 'A' || c == 'B' || c == 'C' || c == 'D' || c == 'E') {
                        endColumnMath = i;
                        startColumnMath = endColumnMath - 39; // 40 questions long
                        break;
                    }
                }
                for (int i = startColumnMath - 1; i >= 0; i--) {
                    char c = line.charAt(i);
                    if (c == 'A' || c == 'B' || c == 'C' || c == 'D' || c == 'E') {
                        endColumnSosyal = i;
                        startColumnSosyal = endColumnSosyal - 19; // 20 questions long TODO: might be 25
                        break;
                    }
                }
                for (int i = startColumnSosyal - 1; i >= 0; i--) {
                    char c = line.charAt(i);
                    if (c == 'A' || c == 'B' || c == 'C' || c == 'D' || c == 'E') {
                        endColumnTurkce = i;
                        startColumnTurkce = endColumnTurkce - 39; // 40 questions long
                        break;
                    }
                }
                startColumnTurkceL.add(startColumnTurkce);
                endColumnTurkceL.add(endColumnTurkce);
                startColumnSosyalL.add(startColumnSosyal);
                endColumnSosyalL.add(endColumnSosyal);
                startColumnMathL.add(startColumnMath);
                endColumnMathL.add(endColumnMath);
                startColumnFenL.add(startColumnFen);
                endColumnFenL.add(endColumnFen);
                Matcher nameMatcher = NAME_PATTERN.matcher(line);
                if (nameMatcher.find()) {
                    startColumnNameL.add(nameMatcher.start());
                    endColumnNameL.add(nameMatcher.end());
                }
                Matcher tcMatcher = TC_PATTERN.matcher(line);
                if (tcMatcher.find()) {
                    startColumnTCL.add(tcMatcher.start());
                    endColumnTCL.add(tcMatcher.end());
                }
                Matcher phoneMatcher = PHONE_PATTERN.matcher(line);
                if (phoneMatcher.find()) {
                    startColumnPhoneL.add(phoneMatcher.start());
                    endColumnPhoneL.add(phoneMatcher.end());
                }
            }
        } catch (IOException e) {
            log.error("Exception occurred while processing text files: {}", e.getMessage(), e);
            throw new RuntimeException("Exception occurred while processing text files: " + e.getMessage(), e);
        }

        // lists are completed now. TODO: check lists to find actual start and end columns

        return "";
    }

    private List<String> extractSections(String line) {
        List<String> sections = new ArrayList<>();
        StringBuilder currentSection = new StringBuilder();
        boolean inAnswerSection = false;
        int answerCount = 0;

        for (char c : line.toCharArray()) {
            if (ANSWER_PATTERN.matcher(String.valueOf(c)).matches()) {
                if (!inAnswerSection) {
                    if (!currentSection.isEmpty()) {
                        sections.add(currentSection.toString());
                    }
                    currentSection = new StringBuilder();
                    inAnswerSection = true;
                }
                currentSection.append(c);
                answerCount++;

                // Check for section breaks based on expected lengths
                if ((sections.isEmpty() && answerCount == 40) || // Turkce
                        (sections.size() == 1 && answerCount == 20) || // Sosyal
                        (sections.size() == 2 && answerCount == 40) || // Matematik
                        (sections.size() == 3 && answerCount == 20)) { // Fen
                    sections.add(currentSection.toString());
                    currentSection = new StringBuilder();
                    answerCount = 0;
                    inAnswerSection = false;
                }
            } else if (inAnswerSection) {
                currentSection.append(c);
            }
        }

        if (!currentSection.isEmpty()) {
            sections.add(currentSection.toString());
        }

        return sections;
    }

    private String extractPhoneNumber(String line) {
        Pattern phonePattern = Pattern.compile("\\d{10,11}");
        Matcher matcher = phonePattern.matcher(line);
        List<String> numbers = new ArrayList<>();

        while (matcher.find()) {
            String number = matcher.group();
            if (number.startsWith("05")) { // Turkish mobile number format
                return number;
            }
            numbers.add(number);
        }

        // If no phone number with "05" prefix found, return empty
        return "";
    }


    private String convertAnswerKeyToCsvAYT(String textContent) {
        StringBuilder csvContent = new StringBuilder();

        // Split content into lines
        String[] lines = textContent.split("\n");

        for (String line : lines) {
            // Remove any leading/trailing whitespace
            line = line.trim();

            if (!line.isEmpty()) {
                line = convertToTurkishChars(line);

                // Split line into parts (assuming space or tab separation in text file)
                String[] parts = line.split("\\s+");

                // Join parts with commas for CSV format
                String csvLine = String.join(",", parts);
                csvContent.append(csvLine).append("\n");
            }
        }

        return csvContent.toString();
    }

    private String convertResultsToCsvAYT(String textContent) {
        StringBuilder csvContent = new StringBuilder();

        // Split content into lines
        String[] lines = textContent.split("\n");

        for (String line : lines) {
            // Remove any leading/trailing whitespace
            line = line.trim();

            if (!line.isEmpty()) {
                line = convertToTurkishChars(line);

                // Split line into parts (assuming space or tab separation in text file)
                String[] parts = line.split("\\s+");

                // Join parts with commas for CSV format
                String csvLine = String.join(",", parts);
                csvContent.append(csvLine).append("\n");
            }
        }

        return csvContent.toString();
    }

    private String convertAnswerKeyToCsvYDT(String textContent) {
        StringBuilder csvContent = new StringBuilder();

        // Split content into lines
        String[] lines = textContent.split("\n");

        for (String line : lines) {
            // Remove any leading/trailing whitespace
            line = line.trim();

            if (!line.isEmpty()) {
                line = convertToTurkishChars(line);

                // Split line into parts (assuming space or tab separation in text file)
                String[] parts = line.split("\\s+");

                // Join parts with commas for CSV format
                String csvLine = String.join(",", parts);
                csvContent.append(csvLine).append("\n");
            }
        }

        return csvContent.toString();
    }

    private String convertResultsToCsvYDT(String textContent) {
        StringBuilder csvContent = new StringBuilder();

        // Split content into lines
        String[] lines = textContent.split("\n");

        for (String line : lines) {
            // Remove any leading/trailing whitespace
            line = line.trim();

            if (!line.isEmpty()) {
                line = convertToTurkishChars(line);

                // Split line into parts (assuming space or tab separation in text file)
                String[] parts = line.split("\\s+");

                // Join parts with commas for CSV format
                String csvLine = String.join(",", parts);
                csvContent.append(csvLine).append("\n");
            }
        }

        return csvContent.toString();
    }

    private String convertAnswerKeyToCsvLGS(String textContent) {
        StringBuilder csvContent = new StringBuilder();

        // Split content into lines
        String[] lines = textContent.split("\n");

        for (String line : lines) {
            // Remove any leading/trailing whitespace
            line = line.trim();

            if (!line.isEmpty()) {
                line = convertToTurkishChars(line);

                // Split line into parts (assuming space or tab separation in text file)
                String[] parts = line.split("\\s+");

                // Join parts with commas for CSV format
                String csvLine = String.join(",", parts);
                csvContent.append(csvLine).append("\n");
            }
        }

        return csvContent.toString();
    }

    private String convertResultsToCsvLGS(String textContent) {
        StringBuilder csvContent = new StringBuilder();

        // Split content into lines
        String[] lines = textContent.split("\n");

        for (String line : lines) {
            // Remove any leading/trailing whitespace
            line = line.trim();

            if (!line.isEmpty()) {
                line = convertToTurkishChars(line);

                // Split line into parts (assuming space or tab separation in text file)
                String[] parts = line.split("\\s+");

                // Join parts with commas for CSV format
                String csvLine = String.join(",", parts);
                csvContent.append(csvLine).append("\n");
            }
        }

        return csvContent.toString();
    }

    private String convertToTurkishChars(String text) {
        return text.replace("Ý", "İ")
                .replace("Þ", "Ş")
                .replace("Ð", "Ğ")
                .replace("ý", "ı")
                .replace("þ", "ş")
                .replace("ð", "ğ")
                .replace("Ã–", "Ö")
                .replace("Ã‡", "Ç")
                .replace("Ã›", "Ü")
                .replace("Ä°", "İ")
                .replace("Ä±", "ı")
                .replace("Ã¶", "ö")
                .replace("Ã§", "ç")
                .replace("Ã¼", "ü")
                .replace("ÄŸ", "ğ")
                .replace("Åž", "Ş")
                .replace("Åž", "ş");
    }

    private String createExamResultDir() {
        try {
            // Create a Path object from the uploadDir
            Path uploadPath = Paths.get(uploadDir);

            // Get the parent directory
            Path parentPath = uploadPath.getParent();

            // Create the new directory "trial_exam_results" in the parent directory
            if (parentPath != null) {
                Path newDir = parentPath.resolve("trial_exam_results");
                // Create the directory if it doesn't exist
                Files.createDirectories(newDir);
                log.info("Trial exam results directory created: {}", newDir);
                return newDir.toString();
            } else {
                log.error("Parent directory not found for upload path: {}", uploadDir);
                throw new IllegalStateException("Could not create trial exam results directory");
            }
        } catch (IOException e) {
            log.error("Failed to create trial exam results directory", e);
            throw new IllegalStateException("Could not create trial exam results directory", e);
        }
    }
}
