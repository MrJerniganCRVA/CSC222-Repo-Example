import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;

public class BattleGUIWorkingExample extends JFrame {

    // ── Model ──────────────────────────────────────────────────────────────
    private Player player;
    private GameCharacter enemy;
    private boolean battleOver = false;
    private int round = 2;

    // ── Palette ────────────────────────────────────────────────────────────
    private static final Color SCENE_BG    = new Color(240, 242, 245); // light gray scene
    private static final Color PLAYER_COL  = new Color(59, 130, 246);  // blue hero
    private static final Color ENEMY_COL   = new Color(239, 68, 68);   // red enemy
    private static final Color HP_GREEN    = new Color(34, 197, 94);
    private static final Color HP_YELLOW   = new Color(250, 204, 21);
    private static final Color HP_RED      = new Color(239, 68, 68);
    private static final Color BTN_ATTACK  = new Color(239, 68, 68);
    private static final Color BTN_EQUIP    = new Color(34, 197, 94);
    private static final Color BTN_RUN     = new Color(107, 114, 128);
    private static final Color PANEL_BG    = new Color(248, 249, 232);  // cream
    private static final Color BORDER_COL  = new Color(40, 40, 40);
    private static final Color LOG_BG      = new Color(230, 240, 210);

    // ── Sprite panels ──────────────────────────────────────────────────────
    private SpritePanel playerSprite;
    private SpritePanel enemySprite;

    // ── HP bars ────────────────────────────────────────────────────────────
    private HPBar playerHPBar;
    private HPBar enemyHPBar;
    private JLabel playerHPLabel;
    private JLabel enemyHPLabel;
    private JLabel playerNameLabel;
    private JLabel enemyNameLabel;
    private JLabel playerPotionLabel;

    // ── Action buttons ─────────────────────────────────────────────────────
    private JButton attackBtn;
    private JButton equipBtn;
    private JButton runBtn;
    private JButton newBattleBtn;

    // ── Combat log ─────────────────────────────────────────────────────────
    private JTextArea combatLog;

    // ── Animation state ────────────────────────────────────────────────────
    private Timer shakeTimer;
    private int shakeCount = 0;

    public BattleGUI(Player player, GameCharacter enemy) {
        this.player = player;
        this.enemy  = enemy;

        setTitle("RPG Battle — Project 3b Demo");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        buildUI();
        updateDisplay();

        pack();
        setLocationRelativeTo(null);
        setVisible(true);

        logMessage("A wild " + enemy.getName() + " appeared!");
        logMessage("Round 1 — choose your action.");
    }

    // ══════════════════════════════════════════════════════════════════════
    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout(0, 0));
        root.setBackground(PANEL_BG);
        setContentPane(root);

        // ── Battle Scene (top half) ───────────────────────────────────────
        JPanel battleScene = buildBattleScene();
        root.add(battleScene, BorderLayout.NORTH);

        // ── Bottom UI (HP bars + buttons + log) ──────────────────────────
        JPanel bottomUI = buildBottomUI();
        root.add(bottomUI, BorderLayout.CENTER);
    }

    // ── Battle Scene ──────────────────────────────────────────────────────
    private JPanel buildBattleScene() {
        JPanel scene = new JPanel(null);
        scene.setBackground(SCENE_BG);
        scene.setBorder(BorderFactory.createMatteBorder(0, 0, 3, 0, BORDER_COL));
        scene.setPreferredSize(new Dimension(680, 200));

        // Enemy sprite — top right
        enemySprite = new SpritePanel(ENEMY_COL, getEnemyShape());
        enemySprite.setBounds(460, 30, 100, 100);
        scene.add(enemySprite);

        // Player sprite — bottom left
        playerSprite = new SpritePanel(PLAYER_COL, "square");
        playerSprite.setBounds(120, 80, 100, 100);
        scene.add(playerSprite);

        return scene;
    }

    // ── Bottom UI ─────────────────────────────────────────────────────────
    private JPanel buildBottomUI() {
        JPanel bottom = new JPanel(new BorderLayout(0, 0));
        bottom.setBackground(PANEL_BG);
        bottom.setBorder(BorderFactory.createMatteBorder(3, 0, 0, 0, BORDER_COL));

        // ── Stat boxes row ────────────────────────────────────────────────
        JPanel statsRow = new JPanel(new GridLayout(1, 2, 4, 0));
        statsRow.setBackground(PANEL_BG);
        statsRow.setBorder(new EmptyBorder(8, 8, 4, 8));
        statsRow.add(buildStatBox(true));   // player
        statsRow.add(buildStatBox(false));  // enemy
        bottom.add(statsRow, BorderLayout.NORTH);

        // ── Action row ────────────────────────────────────────────────────
        JPanel actionRow = new JPanel(new GridLayout(1, 2, 4, 0));
        actionRow.setBackground(PANEL_BG);
        actionRow.setBorder(new EmptyBorder(4, 8, 8, 8));

        // Left: action buttons
        JPanel btnPanel = buildButtonPanel();
        actionRow.add(btnPanel);

        // Right: combat log
        JPanel logPanel = buildLogPanel();
        actionRow.add(logPanel);

        bottom.add(actionRow, BorderLayout.CENTER);

        return bottom;
    }

    private JPanel buildStatBox(boolean isPlayer) {
        JPanel box = new JPanel();
        box.setLayout(new BoxLayout(box, BoxLayout.Y_AXIS));
        box.setBackground(isPlayer ? new Color(220, 235, 255) : new Color(255, 220, 220));
        box.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COL, 2),
                new EmptyBorder(6, 10, 6, 10)
            ));

        JLabel nameLabel = new JLabel(isPlayer ? player.getName() : enemy.getName());
        nameLabel.setFont(new Font("Monospaced", Font.BOLD, 14));
        nameLabel.setForeground(BORDER_COL);
        nameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        HPBar hpBar = new HPBar();
        hpBar.setAlignmentX(Component.LEFT_ALIGNMENT);
        hpBar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 14));

        JLabel hpLabel = new JLabel();
        hpLabel.setFont(new Font("Monospaced", Font.PLAIN, 11));
        hpLabel.setForeground(new Color(60, 60, 60));
        hpLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        box.add(nameLabel);
        box.add(Box.createVerticalStrut(4));
        box.add(hpBar);
        box.add(Box.createVerticalStrut(2));
        box.add(hpLabel);

        if (isPlayer) {
            playerNameLabel = nameLabel;
            playerHPBar = hpBar;
            playerHPLabel = hpLabel;

            playerPotionLabel = new JLabel();
            playerPotionLabel.setFont(new Font("Monospaced", Font.PLAIN, 11));
            playerPotionLabel.setForeground(new Color(34, 120, 60));
            playerPotionLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            box.add(Box.createVerticalStrut(2));
            box.add(playerPotionLabel);
        } else {
            enemyNameLabel = nameLabel;
            enemyHPBar = hpBar;
            enemyHPLabel = hpLabel;
        }

        return box;
    }

    private JPanel buildButtonPanel() {
        JPanel panel = new JPanel(new GridLayout(2, 2, 6, 6));
        panel.setBackground(new Color(200, 220, 180));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COL, 2),
                new EmptyBorder(8, 8, 8, 8)
            ));

        attackBtn  = makeBattleButton("ATTACK",  BTN_ATTACK,  Color.WHITE);
        equipBtn    = makeBattleButton("EQUIP",    BTN_EQUIP,    Color.WHITE);
        runBtn     = makeBattleButton("RUN",     BTN_RUN,     Color.WHITE);
        newBattleBtn = makeBattleButton("NEW",   new Color(99, 102, 241), Color.WHITE);
        newBattleBtn.setEnabled(false);

        attackBtn.addActionListener(e -> handleAttack());
        equipBtn.addActionListener(e -> handleEquip());
        runBtn.addActionListener(e -> handleRun());
        newBattleBtn.addActionListener(e -> resetBattle());

        panel.add(attackBtn);
        panel.add(equipBtn);
        panel.add(runBtn);
        panel.add(newBattleBtn);

        return panel;
    }

    private JPanel buildLogPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(LOG_BG);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COL, 2),
                new EmptyBorder(6, 8, 6, 8)
            ));

        JLabel logTitle = new JLabel("BATTLE LOG");
        logTitle.setFont(new Font("Monospaced", Font.BOLD, 11));
        logTitle.setForeground(new Color(60, 80, 40));
        panel.add(logTitle, BorderLayout.NORTH);

        combatLog = new JTextArea(6, 20);
        combatLog.setEditable(false);
        combatLog.setFont(new Font("Monospaced", Font.PLAIN, 11));
        combatLog.setBackground(LOG_BG);
        combatLog.setForeground(new Color(30, 50, 20));
        combatLog.setWrapStyleWord(true);
        combatLog.setLineWrap(true);
        combatLog.setBorder(null);

        JScrollPane scroll = new JScrollPane(combatLog);
        scroll.setBorder(null);
        scroll.setBackground(LOG_BG);
        panel.add(scroll, BorderLayout.CENTER);

        return panel;
    }

    private void handleAttack() {
        if (battleOver) return;

        // Player attacks enemy
        int dmg = player.attack();
        int playerDmg = enemy.takeDamage(dmg);
        logMessage("Round " + round + " — " + player.getName()
            + " attacks for " + playerDmg + " damage!");

        updateDisplay();

        if (!enemy.isAlive()) {
            shakeSprite(false);
            endBattle(true);
            return;
        }
        int enemyDmg = enemy.attack();
        int actualEnemyDmg = player.takeDamage(enemyDmg);
        logMessage(enemy.getName() + " attacks for "+actualEnemyDmg + " damage!");
        
        if(actualEnemyDmg>=playerDmg){
            shakeSprite(true);
        } else{
            shakeSprite(false);
        }
        
        updateDisplay();
        round++;
        if(!player.isAlive()){
            endBattle(false);
        } else{
            logMessage("Round " + round + " - your move!");
        }
        
        
    }

    private void handleEquip() {
        if (battleOver) return;

        // Build the dialog
        JDialog dialog = new JDialog(this, "Inventory", true); // true = modal
        dialog.setLayout(new BorderLayout());
        dialog.setSize(280, 320);
        dialog.setLocationRelativeTo(this);

        // Title label
        JLabel title = new JLabel("Choose an item to equip:");
        title.setFont(new Font("Monospaced", Font.BOLD, 13));
        title.setBorder(BorderFactory.createEmptyBorder(10, 10, 5, 10));
        dialog.add(title, BorderLayout.NORTH);

        // Item buttons panel
        JPanel itemPanel = new JPanel();
        itemPanel.setLayout(new BoxLayout(itemPanel, BoxLayout.Y_AXIS));
        itemPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

        if (player.getInventory().getItems().isEmpty()) {
            itemPanel.add(new JLabel("Your inventory is empty."));
        } else {
            for (Item item : player.getInventory().getItems()) {
                JButton itemBtn = new JButton(item.getName() + " [" + item.getType() + " +" + item.getValue() + "]");
                itemBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
                itemBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

                // Highlight currently equipped item
                Item currentlyEquipped = player.getInventory().getEquipped();
                if (currentlyEquipped != null && currentlyEquipped.getName().equals(item.getName())) {
                    itemBtn.setText("CURRENTLY EQUIPPED: " + itemBtn.getText());
                    itemBtn.setBackground(new Color(200, 240, 200));
                }

                itemBtn.addActionListener(e -> {
                            if (item.getType() == ItemType.CONSUMABLE) {
                                // Use it instead of equipping
                                player.getInventory().equip(item);
                                int healed = player.heal();
                                if (healed > 0) {
                                    logMessage(player.getName() + " used " + item.getName() + " and healed " + healed + " HP!");
                                    player.getInventory().removeItem(item.getName());
                                } else {
                                    logMessage("Already at full health!");
                                    dialog.dispose();
                                    return; // no turn lost if it didn't do anything
                                }
                            } else {
                                player.getInventory().equip(item.getName());
                                logMessage(player.getName() + " equipped " + item.getName() + "!");
                            }
                            updateDisplay();
                            dialog.dispose();
                            enemyTurn();
                    });

                itemPanel.add(itemBtn);
                itemPanel.add(Box.createVerticalStrut(5));
            }
        }

        JScrollPane scroll = new JScrollPane(itemPanel);
        dialog.add(scroll, BorderLayout.CENTER);

        // Cancel button — closes without doing anything, no turn lost
        JButton cancelBtn = new JButton("Cancel");
        cancelBtn.addActionListener(e -> dialog.dispose());
        JPanel south = new JPanel();
        south.add(cancelBtn);
        dialog.add(south, BorderLayout.SOUTH);

        dialog.setVisible(true); // blocks here until dialog closes (modal)
    }

    private void handleRun() {
        if (battleOver) return;
        logMessage(player.getName() + " fled from battle!");
        logMessage("— You escaped! —");
        endBattle(false);
    }

    private void enemyTurn() {
        int enemyDmg = enemy.attack();
        int actualDmg = player.takeDamage(enemyDmg);
        logMessage(enemy.getName() + " attacks for " + actualDmg + " damage!");
        shakeSprite(true); // shake player
        round++;

        updateDisplay();

        if (!player.isAlive()) {
            endBattle(false);
        } else {
            logMessage("Round " + round + " — your move.");
        }
    }

    private void updateDisplay() {
        // Player HP
        playerHPBar.setPercent(player.getHealthPercent());
        playerHPLabel.setText("HP: " + player.getHealth() + " / " + player.getMaxHealth());
        playerPotionLabel.setText("Current Equipped: " + player.getInventory().getEquipped().getName());

        // Enemy HP
        enemyHPBar.setPercent(enemy.getHealthPercent());
        enemyHPLabel.setText("HP: " + enemy.getHealth() + " / " + enemy.getMaxHealth());

        // Repaint sprites (opacity changes with HP loss)
        playerSprite.setHealthPercent(player.getHealthPercent());
        enemySprite.setHealthPercent(enemy.getHealthPercent());

        repaint();
    }

    private void logMessage(String msg) {
        combatLog.append(msg + "\n");
        combatLog.setCaretPosition(combatLog.getDocument().getLength());
    }

    private void endBattle(boolean playerWon) {
        battleOver = true;
        attackBtn.setEnabled(false);
        equipBtn.setEnabled(false);
        runBtn.setEnabled(false);
        newBattleBtn.setEnabled(true);

        if (playerWon) {
            logMessage("════════════════════");
            logMessage("VICTORY! " + enemy.getName() + " was defeated!");
            logMessage("════════════════════");
            enemySprite.setDefeated(true);
        } else {
            logMessage("════════════════════");
            if (!player.isAlive()) {
                logMessage("DEFEAT... " + player.getName() + " was defeated.");
                playerSprite.setDefeated(true);
            } else {
                logMessage("You escaped safely.");
            }
            logMessage("════════════════════");
        }
    }

    private void resetBattle() {
        // Pick a random enemy for the new battle
        GameCharacter newEnemy = (Math.random() < 0.5) ? new Goblin() : new Dragon();
        this.enemy = newEnemy;

        // Reset player HP (keep their level)
        player.setHealth(player.getMaxHealth());

        battleOver = false;
        round = 1;

        enemySprite.setShape(getEnemyShape());
        enemySprite.setDefeated(false);
        enemySprite.setHealthPercent(100);
        playerSprite.setDefeated(false);
        playerSprite.setHealthPercent(100);

        enemyNameLabel.setText(enemy.getName());
        playerNameLabel.setText(player.getName());

        attackBtn.setEnabled(true);
        equipBtn.setEnabled(true);
        runBtn.setEnabled(true);
        newBattleBtn.setEnabled(false);

        combatLog.setText("");
        updateDisplay();
        logMessage("A new " + enemy.getName() + " appeared!");
        logMessage("Round 1 — choose your action.");
    }

    private String getEnemyShape() {
        if (enemy instanceof Dragon) {
            return "triangle";
        }
        return "circle"; // default for Goblin and anything else
    }

    // ══════════════════════════════════════════════════════════════════════
    // Shake Animation
    // ══════════════════════════════════════════════════════════════════════

    private void shakeSprite(boolean isPlayer) {
        SpritePanel target = isPlayer ? playerSprite : enemySprite;
        Rectangle orig = target.getBounds();
        shakeCount = 0;

        if (shakeTimer != null) shakeTimer.stop();
        shakeTimer = new Timer(40, null);
        shakeTimer.addActionListener(e -> {
                    shakeCount++;
                    int offset = (shakeCount % 2 == 0) ? 6 : -6;
                    target.setLocation(orig.x + offset, orig.y);
                    if (shakeCount >= 8) {
                        target.setLocation(orig.x, orig.y);
                        shakeTimer.stop();
                    }
            });
        shakeTimer.start();
    }

    // ══════════════════════════════════════════════════════════════════════
    // Button Factory
    // ══════════════════════════════════════════════════════════════════════

    private JButton makeBattleButton(String text, Color bg, Color fg) {
        JButton btn = new JButton(text) {
                @Override protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g;
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    if (!isEnabled()) {
                        g2.setColor(new Color(180, 180, 180));
                    } else if (getModel().isPressed()) {
                        g2.setColor(bg.darker());
                    } else if (getModel().isRollover()) {
                        g2.setColor(bg.brighter());
                    } else {
                        g2.setColor(bg);
                    }
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                    g2.setColor(isEnabled() ? fg : Color.LIGHT_GRAY);
                    g2.setFont(getFont());
                    FontMetrics fm = g2.getFontMetrics();
                    int x = (getWidth() - fm.stringWidth(getText())) / 2;
                    int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                    g2.drawString(getText(), x, y);
                }
            };
        btn.setFont(new Font("Monospaced", Font.BOLD, 13));
        btn.setForeground(fg);
        btn.setPreferredSize(new Dimension(120, 48));
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    // Inner class: SpritePanel
    // Draws the character as a plain square (player) or circle (enemy)

    static class SpritePanel extends JPanel {
        private Color baseColor;
        private String shape;
        private int healthPercent = 100;
        private boolean defeated = false;

        SpritePanel(Color color, String shape) {
            this.baseColor = color;
            this.shape = shape;
            setOpaque(false);
        }

        void setShape(String shape)        { this.shape = shape; repaint(); }

        void setHealthPercent(int pct)     { this.healthPercent = pct; repaint(); }

        void setDefeated(boolean d)        { this.defeated = d; repaint(); }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int pad = 8;
            int w = getWidth()  - pad * 2;
            int h = getHeight() - pad * 2;

            // Fade color as HP drops
            float alpha = defeated ? 0.25f : Math.max(0.4f, healthPercent / 100f);
            Color drawColor = new Color(
                    baseColor.getRed(), baseColor.getGreen(), baseColor.getBlue(),
                    (int)(255 * alpha)
                );

            g2.setColor(drawColor);
            if ("triangle".equals(shape)) {
                int[] xp = { pad + w / 2, pad + w, pad };
                int[] yp = { pad,pad + h, pad + h };
                g2.fillPolygon(xp, yp, 3);
                g2.setColor(drawColor.darker());
                g2.setStroke(new BasicStroke(3));
                g2.drawPolygon(xp, yp, 3);
            } else if ("circle".equals(shape)) {
                g2.fillOval(pad, pad, w, h);
                g2.setColor(drawColor.darker());
                g2.setStroke(new BasicStroke(3));
                g2.drawOval(pad, pad, w, h);
            } else {
                // square — player
                g2.fillRect(pad, pad, w, h);
                g2.setColor(drawColor.darker());
                g2.setStroke(new BasicStroke(3));
                g2.drawRect(pad, pad, w, h);
            }

            // Red X if defeated
            if (defeated) {
                g2.setColor(new Color(200, 50, 50, 200));
                g2.setStroke(new BasicStroke(4, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.drawLine(pad + 8, pad + 8, pad + w - 8, pad + h - 8);
                g2.drawLine(pad + w - 8, pad + 8, pad + 8, pad + h - 8);
            }
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    // Inner class: HPBar
    // ══════════════════════════════════════════════════════════════════════

    static class HPBar extends JPanel {
        private int percent = 100;

        HPBar() {
            setPreferredSize(new Dimension(200, 14));
            setOpaque(false);
        }

        void setPercent(int pct) {
            this.percent = Math.max(0, Math.min(100, pct));
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();

            // Background track
            g2.setColor(new Color(180, 180, 180, 120));
            g2.fillRoundRect(0, 0, w, h, h, h);

            // Fill color based on percent
            Color fill = percent > 50 ? HP_GREEN
                : percent > 25 ? HP_YELLOW
                : HP_RED;
            int fillW = (int)(w * (percent / 100.0));
            if (fillW > 0) {
                g2.setColor(fill);
                g2.fillRoundRect(0, 0, fillW, h, h, h);
            }

            // Border
            g2.setColor(new Color(60, 60, 60, 160));
            g2.setStroke(new BasicStroke(1.2f));
            g2.drawRoundRect(0, 0, w - 1, h - 1, h, h);
        }
    }
}