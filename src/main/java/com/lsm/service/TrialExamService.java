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
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.PostConstruct;

@Service
@Slf4j
@RequiredArgsConstructor
public class TrialExamService {

    private final AppUserRepository appUserRepository;
    private final AppUserService appUserService;
    private final PastExamService pastExamService;

    @Value("${app.upload.dir}")
    private String uploadDir;
    private String examResultUploadDir;

    private static final Pattern ANSWER_PATTERN = Pattern.compile("[ABCDE ]+");
    private static final Pattern NAME_PATTERN = Pattern.compile("\\b([a-zA-ZİıĞğÜüÖöŞşÇç]{2,}\\s+[a-zA-ZİıĞğÜüÖöŞşÇç]+'?-?[a-zA-ZİıĞğÜüÖöŞşÇç]{2,}\\s?([a-zA-ZİıĞğÜüŞşÖöÇç]+)?)\\b");
    private static final Pattern TC_PATTERN = Pattern.compile("(?<!\\d)(?!0)\\d{10}[02468](?!\\d)");
    private static final Pattern PHONE_PATTERN = Pattern.compile("(?<!\\d)05\\d{9}(?!\\d)");

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
        textContent = convertToTurkishChars(textContent);
        List<Integer> startColumnNameL   = new ArrayList<>(), endColumnNameL   = new ArrayList<>(),
                      startColumnTCL = new ArrayList<>(), endColumnTCL = new ArrayList<>(),
                      startColumnPhoneL    = new ArrayList<>(), endColumnPhoneL    = new ArrayList<>(),
                      startColumnTurkceL = new ArrayList<>(), endColumnTurkceL = new ArrayList<>(),
                      startColumnSosyalL = new ArrayList<>(), endColumnSosyalL = new ArrayList<>(),
                      startColumnMathL   = new ArrayList<>(), endColumnMathL   = new ArrayList<>(),
                      startColumnFenL    = new ArrayList<>(), endColumnFenL    = new ArrayList<>();

        try {
            BufferedReader reader = new BufferedReader(new StringReader(textContent));
            String line;
            while ((line = reader.readLine()) != null) {
                // line = convertToTurkishChars(line);
                int startColumnTurkce = 0, endColumnTurkce = 0,
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
                        startColumnSosyal = endColumnSosyal - 24; // 20 questions long TODO: might be 25
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
                while (nameMatcher.find()) {
                    String potentialName = nameMatcher.group();
                    if (isTurkishName(potentialName)) {
                        startColumnNameL.add(nameMatcher.start());
                        endColumnNameL.add(nameMatcher.end());
                    }
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
            reader.close();
        } catch (IOException e) {
            log.error("Exception occurred while processing text files: {}", e.getMessage(), e);
            throw new RuntimeException("Exception occurred while processing text files: " + e.getMessage(), e);
        }

        Integer mceName = findMostCommonElement(startColumnNameL);
        Optional<Integer> endNameOpt = endColumnNameL.stream().max(Integer::compare);
        int endName = -1;
        if (endNameOpt.isPresent()) {
            endName = endNameOpt.get();
        }

        Integer mceTc = findMostCommonElement(startColumnTCL);
        Optional<Integer> endTcOpt = endColumnTCL.stream().max(Integer::compare);
        int endTc = -1;
        if (endTcOpt.isPresent()) {
            endTc = endTcOpt.get();
        }

        Integer mcePhone = findMostCommonElement(startColumnPhoneL);
        Optional<Integer> endPhoneOpt = endColumnPhoneL.stream().max(Integer::compare);
        int endPhone = -1;
        if (endPhoneOpt.isPresent()) {
            endName = endPhoneOpt.get();
        }

        Integer mceFen = findMostCommonElement(startColumnFenL);
        Optional<Integer> endFenOpt = endColumnFenL.stream().max(Integer::compare);
        int endFen = -1;
        if (endFenOpt.isPresent()) {
            endFen = endFenOpt.get();
        }

        Integer mceMath = findMostCommonElement(startColumnMathL);
        Optional<Integer> endMathOpt = endColumnMathL.stream().max(Integer::compare);
        int endMath = -1;
        if (endMathOpt.isPresent()) {
            endMath = endMathOpt.get();
        }

        Optional<Integer> mceSosyalOpt = startColumnSosyalL.stream().min(Integer::compare);
        Optional<Integer> endSosyalOpt = endColumnSosyalL.stream().max(Integer::compare);
        int endSosyal = -1, mceSosyal = -1;
        if (endSosyalOpt.isPresent()) {
            endSosyal = endSosyalOpt.get();
        }
        if (mceSosyalOpt.isPresent()) {
            mceSosyal = mceSosyalOpt.get();
        }

        Integer mceTurkce = findMostCommonElement(startColumnTurkceL);
        Optional<Integer> endTurkceOpt = endColumnTurkceL.stream().max(Integer::compare);
        int endTurkce = -1;
        if (endTurkceOpt.isPresent()) {
            endTurkce = endTurkceOpt.get();
        }

        // TODO: validate
        if (endSosyal - mceSosyal < 25) {
            endSosyal = mceSosyal + 25;
        }
        if (endTurkce - mceTurkce > 39) {
            mceTurkce = endTurkce - 39;
        }
        if (endMath - mceMath < 39) {
            endMath = mceMath + 39;
        }
        if (endFen - mceFen > 19) {
            mceFen = endFen - 19;
        }

        // TODO: try to find column of the exam type: A, B, C, D
        // TODO: (which is a column probably only contains one of them, I should only search other columns then line[mce..., end...])
        // Find exam type column
        Map<Integer, Character> examTypeColumns = new HashMap<>();

        try {
            BufferedReader reader = new BufferedReader(new StringReader(textContent));
            String line;
            while ((line = reader.readLine()) != null) {
                line = convertToTurkishChars(line);
                // Skip already identified answer columns
                for (int i = 0; i < line.length(); i++) {
                    if (i >= mceTurkce && i <= endTurkce) continue;
                    if (i >= mceSosyal && i <= endSosyal) continue;
                    if (i >= mceMath && i <= endMath) continue;
                    if (i >= mceFen && i <= endFen) continue;

                    char c = line.charAt(i);
                    if (c == 'A' || c == 'B' || c == 'C' || c == 'D') {
                        examTypeColumns.merge(i, c, (existing, current) ->
                                existing == current ? existing : 'X'); // 'X' marks conflicts
                    }
                }
            }
            reader.close();
        } catch (IOException e) {
            log.error("Exception occurred while finding exam type: {}", e.getMessage(), e);
            throw new RuntimeException("Exception occurred while finding exam type: " + e.getMessage(), e);
        }

        // Find the column that consistently has the same value (A, B, C, or D)
        Integer examTypeColumn = examTypeColumns.entrySet().stream()
                .filter(e -> e.getValue() != 'X')
                .findFirst()
                .map(Map.Entry::getKey)
                .orElse(null);

        // TODO: check it is not class name like 12A
        Character examType = examTypeColumn != null ? examTypeColumns.get(examTypeColumn) : null;

        StringBuilder csv = new StringBuilder();
        String csvHeader = "İsim,Sınıf,TC,Telefon,Kitapçık,Turkce,Sosyal,Matematik,Fen\n";
        csv.append(csvHeader);

        try {
            BufferedReader reader = new BufferedReader(new StringReader(textContent));
            String line;
            while ((line = reader.readLine()) != null) {
                StringBuilder csvLine = new StringBuilder();
                String name = line.substring(mceName, endName);
                String tc = null;
                if (mceTc != null && endTc != -1)
                    tc = line.substring(mceTc, endTc);
                String phone = null;
                if (mcePhone != null && endPhone != -1)
                    phone = line.substring(mcePhone, endPhone);

                AppUser student = appUserRepository.findByNamePlusSurname(name)
                        .orElse(null);
                AppUser studentByTc = null;
                AppUser studentByPhone = null;
                if (student == null) {
                    if (tc != null) {
                        if (tc.length() == 11)
                            studentByTc = appUserRepository.findByStudentDetails_Tc(tc).orElse(null);
                    }
                    if (phone != null) {
                        if (phone.length() == 11)
                            studentByPhone = appUserRepository.findByStudentDetails_Phone(phone).orElse(null);
                    }

                    if (studentByTc == null && studentByPhone == null) {
                        continue;
                    }
                    else if (studentByTc != null && studentByPhone != null) {
                        if (!studentByTc.getId().equals(studentByPhone.getId())) {
                            log.info("Conflict between infos: tc and phone.");
                            continue;
                        } else {
                            student = studentByPhone;
                        }
                    }
                    else if (studentByTc != null) {
                        student = studentByTc;
                    }
                    else {
                        student = studentByPhone;
                    }
                }
                student = appUserService.getCurrentUserWithDetails(student.getId());
                String classEntityName = student.getStudentDetails().getClassEntity().getName();

                // TODO: create PastExam and save it

                String turkce = line.substring(mceTurkce, endTurkce);
                String sosyal = line.substring(mceSosyal, endSosyal);
                String math = line.substring(mceMath, endMath);
                String fen = line.substring(mceFen, endFen);

                csvLine.append(name).append(",");
                csvLine.append(classEntityName).append(",");
                csvLine.append(tc).append(",");
                csvLine.append(phone).append(",");
                csvLine.append(examType).append(",");
                csvLine.append(turkce).append(",");
                csvLine.append(sosyal).append(",");
                csvLine.append(math).append(",");
                csvLine.append(fen).append('\n');

                csv.append(csvLine);
            }
            reader.close();
        } catch (IOException e) {
            log.error("Exception occurred while processing text files: {}", e.getMessage(), e);
            throw new RuntimeException("Exception occurred while processing text files: " + e.getMessage(), e);
        }

        return csv.toString();
    }


    private String convertAnswerKeyToCsvAYT(String textContent) {
        // TODO: to be implemented
        return "";
    }

    private String convertResultsToCsvAYT(String textContent) {
        // TODO: to be implemented
        return "";
    }

    private String convertAnswerKeyToCsvYDT(String textContent) {
        // TODO: to be implemented
        return "";
    }

    private String convertResultsToCsvYDT(String textContent) {
        // TODO: to be implemented
        return "";
    }

    private String convertAnswerKeyToCsvLGS(String textContent) {
        // TODO: to be implemented
        return "";
    }

    private String convertResultsToCsvLGS(String textContent) {
        // TODO: to be implemented
        return "";
    }

    private static <T> T findMostCommonElement(List<T> list) {
        HashMap<T, Integer> countMap = new HashMap<>();

        // Count occurrences of each element
        for (T element : list) {
            countMap.put(element, countMap.getOrDefault(element, 0) + 1);
        }

        // Find the element with the maximum count
        T mostCommonElement = null;
        int maxCount = 0;

        for (Map.Entry<T, Integer> entry : countMap.entrySet()) {
            if (entry.getValue() > maxCount) {
                maxCount = entry.getValue();
                mostCommonElement = entry.getKey();
            }
        }
        return mostCommonElement;
    }

    private String convertToTurkishChars(String text) {
        return text.replaceAll("[ÝЭ]", "İ")
                .replaceAll("ý", "ı")
                .replaceAll("Ц", "Ö")
                .replaceAll("Þ|Åž", "Ş")
                .replaceAll("þ|åž", "ş")
                .replaceAll("Ð|ÄŸ", "Ğ")
                .replaceAll("ð|äŸ", "ğ")
                .replace("Ã–", "Ö")
                .replace("Ã‡", "Ç")
                .replace("Ã›", "Ü")
                .replace("Ä°", "İ")
                .replace("Ä±", "ı")
                .replace("Ã¶", "ö")
                .replace("Ã§", "ç")
                .replace("Ã¼", "ü")
                .replace("ЗERЭBAЮ", "ÇERİBAŞ"); // Special case handling
    }

    private static boolean isTurkishName(String fullName) {
        // If empty or null
        if (fullName == null || fullName.trim().isEmpty()) {
            return false;
        }

        if (fullName.length() < 3 || fullName.length() > 30)
            return false;

        // Split into parts (name and surname)
        String[] nameParts = fullName.trim().split("\\s+");

        // Must have at least 2 parts (name and surname)
        if (nameParts.length < 2) {
            return false;
        }

        // Valid Turkish characters (lowercase and uppercase)
        String turkishChars = "abcçdefgğhıijklmnoöprsştuüvyzABCÇDEFGĞHIİJKLMNOÖPRSŞTUÜVYZ";

        // Check each part
        for (String part : nameParts) {
            // Each part must be at least 2 characters
            if (part.length() < 2) {
                return false;
            }

            // Must start with uppercase
            if (!Character.isUpperCase(part.codePointAt(0))) {
                return false;
            }

            // Check for consecutive ABCDE pattern
            int consecutiveCount = 1;
            for (int i = 0; i < part.length(); i++) {
                char current = Character.toUpperCase(part.charAt(i));

                if (current >= 'A' && current <= 'E') {
                    consecutiveCount++;
                    if (consecutiveCount >= 5) {
                        return false;
                    }
                } else {
                    consecutiveCount = 1;
                }
            }

            // Check if all characters are valid Turkish letters
            for (int i = 0; i < part.length(); i++) {
                if (turkishChars.indexOf(part.charAt(i)) == -1) {
                    return false;
                }
            }
        }

        return true;
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
