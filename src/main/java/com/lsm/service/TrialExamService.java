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
import javax.annotation.PostConstruct;

@Service
@Slf4j
@RequiredArgsConstructor
public class TrialExamService {

    private final AppUserRepository appUserRepository;

    @Value("${app.upload.dir}")
    private String uploadDir;
    private String examResultUploadDir;

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

    public String convertTxtToCsvRaw(String textContent) {
        if (textContent == null || textContent.isEmpty()) {
            return "";
        }

        StringBuilder csvContent = new StringBuilder();
        String[] lines = textContent.split("\n");

        for (String line : lines) {
            if (line.isEmpty()) {
                csvContent.append("\n");
                continue;
            }

            for (int i = 0; i < line.length(); i++) {
                csvContent.append(line.charAt(i));
                if (i < line.length() - 1) {
                    csvContent.append(",");
                }
            }
            csvContent.append("\n");
        }

        return csvContent.toString();
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
                      startColumnTurkceL = new ArrayList<>(), endColumnTurkceL = new ArrayList<>(),
                      startColumnSosyalL = new ArrayList<>(), endColumnSosyalL = new ArrayList<>(),
                      startColumnMathL   = new ArrayList<>(), endColumnMathL   = new ArrayList<>(),
                      startColumnFenL    = new ArrayList<>(), endColumnFenL    = new ArrayList<>();

        BufferedReader reader = new BufferedReader(new StringReader(textContent));
        String line;

        try {
            while ((line = reader.readLine()) != null) {
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
                for(int i = 0; i < line.length(); i++) {
                    char c = line.charAt(i);
                    if (Character.isLetter(c)) {
                        startColumnName = i;
                        endColumnName = i + 1;
                        break;
                    }
                }
            }
        } catch (IOException e) {
            log.error("Exception occurred while processing text files: {}", e.getMessage(), e);
            throw new RuntimeException("Exception occurred while processing text files: " + e.getMessage(), e);
        }

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
