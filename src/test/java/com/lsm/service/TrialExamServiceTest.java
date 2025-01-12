// ./gradlew test --tests "com.lsm.service.TrialExamService"

package com.lsm.service;

import com.lsm.model.entity.ClassEntity;
import com.lsm.model.entity.StudentDetails;
import com.lsm.model.entity.base.AppUser;
import com.lsm.model.entity.enums.Role;
import com.lsm.repository.AppUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.BufferedReader;
import java.io.StringReader;
import java.time.LocalDate;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class TrialExamServiceTest {

    @Mock
    private AppUserRepository appUserRepository;

    @Mock
    private AppUserService appUserService;

    @Mock
    private PastExamService pastExamService;

    private TrialExamService trialExamService;
    private List<AppUser> mockUsers;
    private Map<String, AppUser> userByTC;
    private Map<String, AppUser> userByPhone;
    private Map<String, AppUser> userByName;

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

    @BeforeEach
    void setUp() {
        trialExamService = new TrialExamService(appUserRepository, appUserService, pastExamService);
        ReflectionTestUtils.setField(trialExamService, "uploadDir", "/tmp/upload");

        // Create mock class
        ClassEntity classEntity = ClassEntity.builder()
                .id(1L)
                .name("11A")
                .build();

        // Initialize collections
        mockUsers = new ArrayList<>();
        userByTC = new HashMap<>();
        userByPhone = new HashMap<>();
        userByName = new HashMap<>();

        // Use properly formatted test data
        String testData =
                "1     19802242952          MERT ALЭ ЦLMEZ             BEEBBCDBCB BCADBCEDECDECBAEDCEDAABCADCEDA          BCADECB ECCDBDBAADBB D                            ADADEACBDEBDC    B  CD                            DBACCEDBEEDCEAEBAEBE                                   \n" +
                "1     10640845862          ÇERÝBAÞ ÝSME T             AEBEBDCBCAAEB EDCEBEDCCEBADEDAEDACBDAECAD          BCDAEDCCB C ADBABEBC                              DDEABDDCADEDBCCBCC E D B EBCC CBDB C E            BBBDCECBEEEABCED CBE \n" +
                "1     24800076300          IRMAK ÇELÝK                BEEBBCDB    EA BCEAECDECBAEDCEDDABCADCEDA            B   E   E   BA BB                                DE A  EB   A       C                             BB D E B D                                             \n" +
                "1     24005104938          ELA YANARDAÐ               AEBEBDCBCAD BAED EBEDCCEBADEC EDAC DAECAD          BBE   CCA C E  AECBEBCBAC                               E A        C                    E           D B    C  C        A                                   \n" +
                "1     33418749896          SÖKER TIRPANCI ÝREM        AEBEBDCBBADEBADDCEBEDCCEBADECAADACBDAECAD            DDEDDCBAEBADBABEBE                              ADEBBDDEADDDBCC  CAE D AEEBCB CBDBE  E            A BDC CBCDEEBAEDECBE                                   \n";

        try (BufferedReader reader = new BufferedReader(new StringReader(testData))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = convertToTurkishChars(line);
                String tc = line.substring(6, 17).trim();
                String fullName = convertToTurkishChars(line.substring(27, 46).trim());

                String[] nameParts = fullName.split(" ");
                String surname = nameParts[nameParts.length - 1];
                String name = String.join(" ", Arrays.copyOf(nameParts, nameParts.length - 1));

                // Create mock phone
                String phone = "0555" + tc.substring(0, 7);

                StudentDetails studentDetails = StudentDetails.builder()
                        .tc(tc)
                        .phone(phone)
                        .classEntity(classEntity)
                        .birthDate(LocalDate.now().minusYears(17))
                        .registrationDate(LocalDate.now().minusMonths(6))
                        .build();

                AppUser user = AppUser.builder()
                        .id(Long.parseLong(tc.substring(0, 5)))
                        .username(name.toLowerCase().replace(" ", "") + surname.toLowerCase())
                        .name(name)
                        .surname(surname)
                        .email(name.toLowerCase().replace(" ", "") + "." +
                                surname.toLowerCase() + "@example.com")
                        .password("securePassword123_@_@")
                        .role(Role.ROLE_STUDENT)
                        .studentDetails(studentDetails)
                        .build();

                mockUsers.add(user);
                userByTC.put(tc, user);
                userByPhone.put(phone, user);
                userByName.put(fullName, user);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to create mock users", e);
        }

        // Setup repository mocks
        when(appUserRepository.findByNamePlusSurname(anyString()))
                .thenAnswer(invocation -> {
                    String name = convertToTurkishChars(invocation.getArgument(0));
                    return Optional.ofNullable(userByName.get(name));
                });

        when(appUserRepository.getByStudentDetails_Tc(anyString()))
                .thenAnswer(invocation -> {
                    String tc = invocation.getArgument(0);
                    return Optional.ofNullable(userByTC.get(tc));
                });

        when(appUserRepository.getByStudentDetails_phone(anyString()))
                .thenAnswer(invocation -> {
                    String phone = invocation.getArgument(0);
                    return Optional.ofNullable(userByPhone.get(phone));
                });

        when(appUserService.getCurrentUserWithDetails(any()))
                .thenAnswer(invocation -> {
                    Long id = invocation.getArgument(0);
                    return mockUsers.stream()
                            .filter(u -> u.getId().equals(id))
                            .findFirst()
                            .orElseThrow(() -> new RuntimeException("User not found"));
                });
    }

    @Test
    void testConvertResultsToCsvTYT_WithRealData() {
        // Single line test with properly formatted data
        String testInput = "1     19802242952          MERT ALЭ ЦLMEZ             BEEBBCDBCB BCADBCEDECDECBAEDCEDAABCADCEDA          BCADECB ECCDBDBAADBB D                            ADADEACBDEBDC    B  CD                            DBACCEDBEEDCEAEBAEBE                                   \n" +
                "1     10640845862          ÇERÝBAÞ ÝSME T             AEBEBDCBCAAEB EDCEBEDCCEBADEDAEDACBDAECAD          BCDAEDCCB C ADBABEBC                              DDEABDDCADEDBCCBCC E D B EBCC CBDB C E            BBBDCECBEEEABCED CBE \n" +
                "1     24800076300          IRMAK ÇELÝK                BEEBBCDB    EA BCEAECDECBAEDCEDDABCADCEDA            B   E   E   BA BB                                DE A  EB   A       C                             BB D E B D                                             \n" +
                "1     24005104938          ELA YANARDAÐ               AEBEBDCBCAD BAED EBEDCCEBADEC EDAC DAECAD          BBE   CCA C E  AECBEBCBAC                               E A        C                    E           D B    C  C        A                                   \n" +
                "1     33418749896          SÖKER TIRPANCI ÝREM        AEBEBDCBBADEBADDCEBEDCCEBADECAADACBDAECAD            DDEDDCBAEBADBABEBE                              ADEBBDDEADDDBCC  CAE D AEEBCB CBDBE  E            A BDC CBCDEEBAEDECBE                                   \n";

        System.out.println("\n=== INPUT DATA ===");
        System.out.println(testInput);

        testInput = convertToTurkishChars(testInput);

        // Print what's being found by NAME_PATTERN
        Pattern NAME_PATTERN = Pattern.compile("\\b([a-zA-ZİıĞğÜüÖöŞşÇç]{2,}\\s+[a-zA-ZİıĞğÜüÖöŞşÇç]+'?-?[a-zA-ZİıĞğÜüÖöŞşÇç]{2,}\\s?([a-zA-ZİıĞğÜüŞşÖöÇç]{1,})?)\\b");
        Matcher matcher = NAME_PATTERN.matcher(testInput);
        System.out.println("\n=== NAME MATCHES ===");
        while (matcher.find()) {
            String potentialName = matcher.group();
            // System.out.println("Found potential name: [" + potentialName + "] at position " + matcher.start() + "-" + matcher.end());
            if (isTurkishName(potentialName)) {
                System.out.println("Found name: [" + potentialName + "] at position " + matcher.start() + "-" + matcher.end());
            }
        }

        Pattern TC_PATTERN = Pattern.compile("(?<!\\d)(?!0)\\d{10}[02468](?!\\d)");
        Matcher matcherTc = TC_PATTERN.matcher(testInput);
        System.out.println("\n=== TC MATCHES ===");
        while (matcherTc.find()) {
            System.out.println("Found TC: [" + matcherTc.group() + "] at position " + matcherTc.start() + "-" + matcherTc.end());
        }

        // Convert and print result
        String result = ReflectionTestUtils.invokeMethod(trialExamService,
                "convertResultsToCsvTYT",
                testInput);

        System.out.println("\n=== CONVERSION RESULT ===");
        System.out.println(result);

        System.out.println("\n=== VERIFICATION ===");
        System.out.println("Contains 'MERT ALİ ÖLMEZ': " + result.contains("MERT ALİ ÖLMEZ"));
        System.out.println("Contains '19802242952': " + result.contains("19802242952"));
        System.out.println("Contains '11A': " + result.contains("11A"));

        String[] lines = result.split("\n");
        System.out.println("\n=== CSV LINES (" + lines.length + " lines) ===");
        for (int i = 0; i < lines.length; i++) {
            System.out.println("Line " + i + ": " + lines[i]);
        }

        assertNotNull(result);
        assertTrue(result.contains("MERT ALİ ÖLMEZ"));
        assertTrue(result.contains("19802242952"));
        assertTrue(result.contains("11A"));

        assertTrue(lines.length >= 2);
        assertEquals("İsim,Sınıf,TC,Telefon,Kitapçık,Turkce,Sosyal,Matematik,Fen", lines[0]);
    }

    private String generateAnswers(int count) {
        StringBuilder answers = new StringBuilder();
        String[] options = {"A", "B", "C", "D", "E"};
        for (int i = 0; i < count; i++) {
            answers.append(options[i % 5]);
        }
        answers.append(" ".repeat(Math.max(0, 40 - answers.length())));
        return answers.toString();
    }
}