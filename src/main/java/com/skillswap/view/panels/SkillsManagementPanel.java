package com.skillswap.view.panels;

import com.skillswap.util.UIConstants;
import com.skillswap.view.components.CardContainer;
import com.skillswap.view.components.SkillItemCard;
import com.skillswap.view.components.StyledButton;
import com.skillswap.view.components.StyledTextField;
import com.skillswap.view.components.WrapLayout;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Modern dark-themed view for managing skills to teach and skills to learn.
 *
 * <p>Supports:
 * <ul>
 *   <li>Adding new skills with input validation and duplicate prevention</li>
 *   <li>Editing existing skills with dialog prompt</li>
 *   <li>Deleting skills with confirmation</li>
 *   <li>Visual card/tag representation with distinct theme coloring</li>
 *   <li>Empty, loading, and error/success states</li>
 * </ul>
 */
public class SkillsManagementPanel extends JPanel {

    private final StyledTextField teachInputField;
    private final StyledButton btnAddTeach;
    private final JPanel teachCardsContainer;
    private final JLabel emptyTeachLabel;

    private final StyledTextField learnInputField;
    private final StyledButton btnAddLearn;
    private final JPanel learnCardsContainer;
    private final JLabel emptyLearnLabel;

    private final JLabel messageLabel;

    private List<String> currentTeachList = new ArrayList<>();
    private List<String> currentLearnList = new ArrayList<>();

    // Action listeners
    private Consumer<String> onAddTeachListener;
    private BiConsumer<String, String> onEditTeachListener;
    private Consumer<String> onDeleteTeachListener;

    private Consumer<String> onAddLearnListener;
    private BiConsumer<String, String> onEditLearnListener;
    private Consumer<String> onDeleteLearnListener;

    public SkillsManagementPanel() {
        setLayout(new BorderLayout());
        setBackground(UIConstants.BG_PRIMARY);
        setBorder(new EmptyBorder(28, 32, 28, 32));

        // 1. Header Section
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        headerPanel.setBorder(new EmptyBorder(0, 0, 16, 0));

        JLabel title = new JLabel("💡 My Skills & Learning Goals");
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setForeground(UIConstants.TEXT_PRIMARY);

        JLabel subtitle = new JLabel("Manage what you can teach fellow students and what topics you are looking to master");
        subtitle.setFont(UIConstants.FONT_BODY);
        subtitle.setForeground(UIConstants.TEXT_SECONDARY);

        JPanel titleBox = new JPanel();
        titleBox.setLayout(new BoxLayout(titleBox, BoxLayout.Y_AXIS));
        titleBox.setOpaque(false);
        titleBox.add(title);
        titleBox.add(Box.createVerticalStrut(4));
        titleBox.add(subtitle);
        headerPanel.add(titleBox, BorderLayout.WEST);

        add(headerPanel, BorderLayout.NORTH);

        // 2. Center Content - Scrollable Box
        JPanel mainContent = new JPanel();
        mainContent.setLayout(new BoxLayout(mainContent, BoxLayout.Y_AXIS));
        mainContent.setOpaque(false);

        // Feedback / Notification Banner
        messageLabel = new JLabel(" ");
        messageLabel.setFont(UIConstants.FONT_SMALL);
        messageLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        mainContent.add(messageLabel);
        mainContent.add(Box.createVerticalStrut(12));

        // SECTION 1: Skills I Can Teach
        CardContainer teachCard = new CardContainer(new EmptyBorder(22, 24, 22, 24));
        teachCard.setLayout(new BoxLayout(teachCard, BoxLayout.Y_AXIS));
        teachCard.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel teachSectionTitle = new JLabel("💡 Skills I Can Teach");
        teachSectionTitle.setFont(new Font("Segoe UI", Font.BOLD, 17));
        teachSectionTitle.setForeground(UIConstants.ACCENT);

        JLabel teachSectionDesc = new JLabel("Subjects, frameworks, languages, or tools you can mentor or share with others.");
        teachSectionDesc.setFont(UIConstants.FONT_SMALL);
        teachSectionDesc.setForeground(UIConstants.TEXT_SECONDARY);

        teachCard.add(teachSectionTitle);
        teachCard.add(Box.createVerticalStrut(4));
        teachCard.add(teachSectionDesc);
        teachCard.add(Box.createVerticalStrut(16));

        // Teach Input Row
        JPanel teachInputRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        teachInputRow.setOpaque(false);
        teachInputRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        teachInputField = new StyledTextField("e.g. Java, Data Structures, Python, UI Design");
        teachInputField.setPreferredSize(new Dimension(380, UIConstants.INPUT_HEIGHT));
        teachInputField.addActionListener(e -> triggerAddTeach());

        btnAddTeach = new StyledButton("+ Add Skill", StyledButton.ButtonStyle.PRIMARY);
        btnAddTeach.setPreferredSize(new Dimension(140, UIConstants.INPUT_HEIGHT));
        btnAddTeach.addActionListener(e -> triggerAddTeach());

        teachInputRow.add(teachInputField);
        teachInputRow.add(btnAddTeach);
        teachCard.add(teachInputRow);
        teachCard.add(Box.createVerticalStrut(18));

        // Teach Cards Container
        teachCardsContainer = new JPanel(new WrapLayout(FlowLayout.LEFT, 10, 10));
        teachCardsContainer.setOpaque(false);
        teachCardsContainer.setAlignmentX(Component.LEFT_ALIGNMENT);

        emptyTeachLabel = new JLabel("No teaching skills added yet. Add your first skill above!");
        emptyTeachLabel.setFont(UIConstants.FONT_BODY);
        emptyTeachLabel.setForeground(UIConstants.TEXT_HINT);

        teachCardsContainer.add(emptyTeachLabel);
        teachCard.add(teachCardsContainer);

        mainContent.add(teachCard);
        mainContent.add(Box.createVerticalStrut(24));

        // SECTION 2: Skills I Want to Learn
        CardContainer learnCard = new CardContainer(new EmptyBorder(22, 24, 22, 24));
        learnCard.setLayout(new BoxLayout(learnCard, BoxLayout.Y_AXIS));
        learnCard.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel learnSectionTitle = new JLabel("🎯 Skills I Want to Learn");
        learnSectionTitle.setFont(new Font("Segoe UI", Font.BOLD, 17));
        learnSectionTitle.setForeground(UIConstants.ACCENT_GREEN);

        JLabel learnSectionDesc = new JLabel("Topics, concepts, or tools you are seeking peer exchange or study partners for.");
        learnSectionDesc.setFont(UIConstants.FONT_SMALL);
        learnSectionDesc.setForeground(UIConstants.TEXT_SECONDARY);

        learnCard.add(learnSectionTitle);
        learnCard.add(Box.createVerticalStrut(4));
        learnCard.add(learnSectionDesc);
        learnCard.add(Box.createVerticalStrut(16));

        // Learn Input Row
        JPanel learnInputRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        learnInputRow.setOpaque(false);
        learnInputRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        learnInputField = new StyledTextField("e.g. Machine Learning, React, Cloud Computing, DevOps");
        learnInputField.setPreferredSize(new Dimension(380, UIConstants.INPUT_HEIGHT));
        learnInputField.addActionListener(e -> triggerAddLearn());

        btnAddLearn = new StyledButton("+ Add Interest", StyledButton.ButtonStyle.PRIMARY);
        btnAddLearn.setPreferredSize(new Dimension(140, UIConstants.INPUT_HEIGHT));
        btnAddLearn.addActionListener(e -> triggerAddLearn());

        learnInputRow.add(learnInputField);
        learnInputRow.add(btnAddLearn);
        learnCard.add(learnInputRow);
        learnCard.add(Box.createVerticalStrut(18));

        // Learn Cards Container
        learnCardsContainer = new JPanel(new WrapLayout(FlowLayout.LEFT, 10, 10));
        learnCardsContainer.setOpaque(false);
        learnCardsContainer.setAlignmentX(Component.LEFT_ALIGNMENT);

        emptyLearnLabel = new JLabel("No learning interests added yet. Add what you want to learn above!");
        emptyLearnLabel.setFont(UIConstants.FONT_BODY);
        emptyLearnLabel.setForeground(UIConstants.TEXT_HINT);

        learnCardsContainer.add(emptyLearnLabel);
        learnCard.add(learnCardsContainer);

        mainContent.add(learnCard);

        // Put into JScrollPane
        JScrollPane scrollPane = new JScrollPane(mainContent);
        scrollPane.setBorder(null);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.getVerticalScrollBar().setUnitIncrement(14);

        add(scrollPane, BorderLayout.CENTER);
    }

    private void triggerAddTeach() {
        String skill = teachInputField.getText() != null ? teachInputField.getText().trim() : "";
        if (skill.isEmpty()) {
            setErrorMessage("Please enter a skill to teach.");
            return;
        }
        if (onAddTeachListener != null) {
            onAddTeachListener.accept(skill);
        }
    }

    private void triggerAddLearn() {
        String skill = learnInputField.getText() != null ? learnInputField.getText().trim() : "";
        if (skill.isEmpty()) {
            setErrorMessage("Please enter a skill you want to learn.");
            return;
        }
        if (onAddLearnListener != null) {
            onAddLearnListener.accept(skill);
        }
    }

    public void clearTeachInput() {
        teachInputField.setText("");
    }

    public void clearLearnInput() {
        learnInputField.setText("");
    }

    /**
     * Updates the displayed teaching skills.
     */
    public void setTeachSkills(List<String> skills) {
        this.currentTeachList = (skills != null) ? new ArrayList<>(skills) : new ArrayList<>();
        teachCardsContainer.removeAll();

        if (currentTeachList.isEmpty()) {
            teachCardsContainer.add(emptyTeachLabel);
        } else {
            for (String skill : currentTeachList) {
                SkillItemCard card = new SkillItemCard(
                        skill,
                        SkillItemCard.Type.TEACH,
                        this::handleEditTeach,
                        this::handleDeleteTeach
                );
                teachCardsContainer.add(card);
            }
        }

        teachCardsContainer.revalidate();
        teachCardsContainer.repaint();
    }

    /**
     * Updates the displayed learning skills.
     */
    public void setLearnSkills(List<String> skills) {
        this.currentLearnList = (skills != null) ? new ArrayList<>(skills) : new ArrayList<>();
        learnCardsContainer.removeAll();

        if (currentLearnList.isEmpty()) {
            learnCardsContainer.add(emptyLearnLabel);
        } else {
            for (String skill : currentLearnList) {
                SkillItemCard card = new SkillItemCard(
                        skill,
                        SkillItemCard.Type.LEARN,
                        this::handleEditLearn,
                        this::handleDeleteLearn
                );
                learnCardsContainer.add(card);
            }
        }

        learnCardsContainer.revalidate();
        learnCardsContainer.repaint();
    }

    private void handleEditTeach(String oldSkill) {
        String newSkill = (String) JOptionPane.showInputDialog(
                this,
                "Edit teaching skill:",
                "Edit Skill",
                JOptionPane.PLAIN_MESSAGE,
                null,
                null,
                oldSkill
        );

        if (newSkill != null) {
            newSkill = newSkill.trim();
            if (newSkill.isEmpty()) {
                setErrorMessage("Skill name cannot be empty.");
                return;
            }
            if (newSkill.equalsIgnoreCase(oldSkill)) {
                return; // Unchanged
            }
            if (onEditTeachListener != null) {
                onEditTeachListener.accept(oldSkill, newSkill);
            }
        }
    }

    private void handleDeleteTeach(String skill) {
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to remove \"" + skill + "\" from your teaching skills?",
                "Confirm Removal",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );

        if (confirm == JOptionPane.YES_OPTION && onDeleteTeachListener != null) {
            onDeleteTeachListener.accept(skill);
        }
    }

    private void handleEditLearn(String oldSkill) {
        String newSkill = (String) JOptionPane.showInputDialog(
                this,
                "Edit learning interest:",
                "Edit Skill",
                JOptionPane.PLAIN_MESSAGE,
                null,
                null,
                oldSkill
        );

        if (newSkill != null) {
            newSkill = newSkill.trim();
            if (newSkill.isEmpty()) {
                setErrorMessage("Skill name cannot be empty.");
                return;
            }
            if (newSkill.equalsIgnoreCase(oldSkill)) {
                return; // Unchanged
            }
            if (onEditLearnListener != null) {
                onEditLearnListener.accept(oldSkill, newSkill);
            }
        }
    }

    private void handleDeleteLearn(String skill) {
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to remove \"" + skill + "\" from your learning goals?",
                "Confirm Removal",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );

        if (confirm == JOptionPane.YES_OPTION && onDeleteLearnListener != null) {
            onDeleteLearnListener.accept(skill);
        }
    }

    public void setSuccessMessage(String message) {
        messageLabel.setForeground(UIConstants.ACCENT_GREEN);
        messageLabel.setText("✓ " + message);
    }

    public void setErrorMessage(String message) {
        messageLabel.setForeground(UIConstants.ACCENT_RED);
        messageLabel.setText("✗ " + message);
    }

    public void clearMessage() {
        messageLabel.setText(" ");
    }

    public void setLoading(boolean loading) {
        btnAddTeach.setLoading(loading);
        btnAddLearn.setLoading(loading);
        teachInputField.setEnabled(!loading);
        learnInputField.setEnabled(!loading);
    }

    public List<String> getCurrentTeachList() {
        return new ArrayList<>(currentTeachList);
    }

    public List<String> getCurrentLearnList() {
        return new ArrayList<>(currentLearnList);
    }

    // Listener setters
    public void setOnAddTeach(Consumer<String> listener) {
        this.onAddTeachListener = listener;
    }

    public void setOnEditTeach(BiConsumer<String, String> listener) {
        this.onEditTeachListener = listener;
    }

    public void setOnDeleteTeach(Consumer<String> listener) {
        this.onDeleteTeachListener = listener;
    }

    public void setOnAddLearn(Consumer<String> listener) {
        this.onAddLearnListener = listener;
    }

    public void setOnEditLearn(BiConsumer<String, String> listener) {
        this.onEditLearnListener = listener;
    }

    public void setOnDeleteLearn(Consumer<String> listener) {
        this.onDeleteLearnListener = listener;
    }
}
