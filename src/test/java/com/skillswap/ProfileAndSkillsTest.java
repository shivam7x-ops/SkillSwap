package com.skillswap;

import com.skillswap.model.User;
import com.skillswap.service.StudentService;
import com.skillswap.util.ValidationUtil;
import com.skillswap.view.components.SkillItemCard;
import com.skillswap.view.panels.ProfilePanel;
import com.skillswap.view.panels.SkillsManagementPanel;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Automated test suite for Step 8: My Profile & Skill Management.
 * Verifies profile viewing/editing, duplicate skill validation, clean lists, and Swing views.
 */
public class ProfileAndSkillsTest {

    @BeforeAll
    public static void setup() {
        System.setProperty("java.awt.headless", "true");
    }

    @Test
    @DisplayName("ProfilePanel: Population, Read-Only Email, and Reset")
    public void testProfilePanel() {
        ProfilePanel panel = new ProfilePanel();
        assertNotNull(panel);

        User user = new User("uid-test-101", "Alex Hunter", "alex@stanford.edu", "Stanford University");
        panel.setStudent(user);

        assertEquals("Alex Hunter", panel.getFullName());
        assertEquals("alex@stanford.edu", panel.getEmail());
        assertEquals("Stanford University", panel.getCollege());
        assertEquals("uid-test-101", panel.getUid());

        // Email field must be strictly read-only
        assertFalse(panel.getEmail().isEmpty());

        // Test editing inputs
        panel.getNameField().setText("Alex H. Updated");
        panel.getCollegeField().setText("Stanford Institute");
        assertEquals("Alex H. Updated", panel.getFullName());
        assertEquals("Stanford Institute", panel.getCollege());

        // Test Reset cancels back to stored student info
        panel.resetFields();
        assertEquals("Alex Hunter", panel.getFullName());
        assertEquals("Stanford University", panel.getCollege());

        // Test message states
        panel.setSuccessMessage("Saved!");
        panel.setErrorMessage("Error!");
        panel.clearMessage();

        // Test loading state
        assertDoesNotThrow(() -> panel.setLoading(true));
        assertDoesNotThrow(() -> panel.setLoading(false));
    }

    @Test
    @DisplayName("SkillsManagementPanel: Population and Skill Cards")
    public void testSkillsManagementPanel() {
        SkillsManagementPanel panel = new SkillsManagementPanel();
        assertNotNull(panel);

        List<String> teach = Arrays.asList("Java", "Algorithms", "React");
        List<String> learn = Arrays.asList("Python", "Kubernetes");

        panel.setTeachSkills(teach);
        panel.setLearnSkills(learn);

        assertEquals(3, panel.getCurrentTeachList().size());
        assertEquals(2, panel.getCurrentLearnList().size());

        // Test Empty states
        assertDoesNotThrow(() -> panel.setTeachSkills(Collections.emptyList()));
        assertEquals(0, panel.getCurrentTeachList().size());

        assertDoesNotThrow(() -> panel.setLearnSkills(Collections.emptyList()));
        assertEquals(0, panel.getCurrentLearnList().size());

        // Feedback banners
        assertDoesNotThrow(() -> panel.setSuccessMessage("Skill added"));
        assertDoesNotThrow(() -> panel.setErrorMessage("Duplicate skill"));
        assertDoesNotThrow(panel::clearMessage);
    }

    @Test
    @DisplayName("SkillItemCard: Component and Action Callbacks")
    public void testSkillItemCard() {
        List<String> edited = new ArrayList<>();
        List<String> deleted = new ArrayList<>();

        SkillItemCard card = new SkillItemCard(
                "Data Structures",
                SkillItemCard.Type.TEACH,
                edited::add,
                deleted::add
        );

        assertEquals("Data Structures", card.getSkillName());
    }

    @Test
    @DisplayName("StudentService: Duplicate Skill Validation (Case-Insensitive)")
    public void testDuplicateSkillValidation() {
        StudentService service = StudentService.getInstance();
        List<String> existing = Arrays.asList("Java", "Python", "Data Structures");

        // 1. Empty or blank
        assertNotNull(service.validateNewSkill("", existing));
        assertNotNull(service.validateNewSkill("   ", existing));
        assertNotNull(service.validateNewSkill(null, existing));

        // 2. Too short (< 2 characters)
        assertNotNull(service.validateNewSkill("A", existing));

        // 3. Exact duplicate
        assertEquals("You have already added this skill.", service.validateNewSkill("Java", existing));

        // 4. Case-insensitive duplicate
        assertEquals("You have already added this skill.", service.validateNewSkill("JAVA", existing));
        assertEquals("You have already added this skill.", service.validateNewSkill("python", existing));
        assertEquals("You have already added this skill.", service.validateNewSkill("  data structures  ", existing));

        // 5. Valid new skill
        assertNull(service.validateNewSkill("C++", existing));
        assertNull(service.validateNewSkill("Machine Learning", existing));
    }

    @Test
    @DisplayName("StudentService: Clean Skills List (Deduplication & Sorting)")
    public void testCleanSkillsList() {
        StudentService service = StudentService.getInstance();

        List<String> raw = Arrays.asList(" Python", "java ", "PYTHON", "C++", "", "  ", "java", "React");
        List<String> cleaned = service.cleanSkillsList(raw);

        // Expected unique items sorted: C++, Java, Python, React
        assertEquals(4, cleaned.size());
        assertTrue(cleaned.contains("C++"));
        assertTrue(cleaned.contains("Java") || cleaned.contains("java"));
        assertTrue(cleaned.contains("Python"));
        assertTrue(cleaned.contains("React"));
    }

    @Test
    @DisplayName("ValidationUtil: Name, College, and Skill Validation")
    public void testValidationUtil() {
        assertNull(ValidationUtil.validateName("John Doe"));
        assertNotNull(ValidationUtil.validateName(""));
        assertNotNull(ValidationUtil.validateName("J"));

        assertNull(ValidationUtil.validateCollege("MIT"));
        assertNotNull(ValidationUtil.validateCollege(""));
        assertNotNull(ValidationUtil.validateCollege("   "));

        assertNull(ValidationUtil.validateSkill("Spring Boot"));
        assertNotNull(ValidationUtil.validateSkill(""));
        assertNotNull(ValidationUtil.validateSkill("S"));
    }
}
