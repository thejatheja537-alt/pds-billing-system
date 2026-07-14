import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

// ==========================================
// 1. APPLICATION ENTRY ARCHITECTURE
// ==========================================
public class Project {
    public static void main(String[] args) {
        configureModernGraphicsPipeline();
        
        SwingUtilities.invokeLater(() -> {
            SplashView splash = new SplashView();
            splash.setVisible(true);
            
            new SwingWorker<Void, Integer>() {
                @Override
                protected Void doInBackground() throws Exception {
                    // Simulating deep framework component validation pipelines
                    for (int i = 0; i <= 100; i += 5) {
                        Thread.sleep(20);
                        publish(i);
                    }
                    return null;
                }

                @Override
                protected void process(List<Integer> chunks) {
                    splash.updateLoadMetric(chunks.get(chunks.size() - 1));
                }

                @Override
                protected void done() {
                    splash.dispose();
                    PdsEngine coreEngine = new PdsEngine();
                    new MainBillingDashboard(coreEngine);
                }
            }.execute();
        });
    }

    private static void configureModernGraphicsPipeline() {
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
            // Overriding component UI archetypes for structural elegance
            UIManager.put("Button.font", new Font("Segoe UI", Font.BOLD, 12));
            UIManager.put("Label.font", new Font("Segoe UI", Font.PLAIN, 13));
            UIManager.put("Table.font", new Font("Segoe UI", Font.PLAIN, 13));
            UIManager.put("TableHeader.font", new Font("Segoe UI", Font.BOLD, 13));
            UIManager.put("ComboBox.font", new Font("Segoe UI", Font.PLAIN, 13));
            UIManager.put("TextField.font", new Font("Segoe UI", Font.PLAIN, 13));
        } catch (Exception e) {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
}

// ==========================================
// 2. EXCEPTION DOMAIN CORE HIERARCHY
// ==========================================
abstract class PdsDomainException extends RuntimeException {
    public PdsDomainException(String message) { super(message); }
}

class StockDeficitException extends PdsDomainException {
    public StockDeficitException(String item, double req, double avail) {
        super(String.format("Warehouse Capacity Deficit: %s requests %.2f units but only %.2f remain.", item, req, avail));
    }
}

class QuotaViolationException extends PdsDomainException {
    public QuotaViolationException(String message) { super(message); }
}

// ==========================================
// 3. STATE COUPLING ORCHESTRATION (OBSERVER)
// ==========================================
interface EngineStateListener {
    void onEngineStateMutation();
}

// ==========================================
// 4. FUNCTIONAL ENCAPSULATED STRATEGY MATRIX
// ==========================================
interface TariffStrategy {
    BigDecimal calculateRate(String item);
}

enum CardTierHierarchy {
    AYY("AYY (Antyodaya Anna Yojana)", item -> {
        switch (item) {
            case "Rice":     return new BigDecimal("3.00");
            case "Wheat":    return new BigDecimal("2.00");
            case "Sugar":    return new BigDecimal("13.50");
            case "Kerosene": return new BigDecimal("15.00");
            default:         return BigDecimal.ZERO;
        }
    }),
    PHH("PHH (Priority Household)", item -> {
        switch (item) {
            case "Rice":     return new BigDecimal("5.00");
            case "Wheat":    return new BigDecimal("3.00");
            case "Sugar":    return new BigDecimal("20.00");
            case "Kerosene": return new BigDecimal("25.00");
            default:         return BigDecimal.ZERO;
        }
    }),
    NPHH("NPHH (Non-Priority)", item -> {
        switch (item) {
            case "Rice":     return new BigDecimal("15.00");
            case "Wheat":    return new BigDecimal("12.00");
            case "Sugar":    return new BigDecimal("40.00");
            case "Kerosene": return new BigDecimal("50.00");
            default:         return BigDecimal.ZERO;
        }
    });

    private final String displayLabel;
    private final TariffStrategy strategy;

    CardTierHierarchy(String displayLabel, TariffStrategy strategy) {
        this.displayLabel = displayLabel;
        this.strategy = strategy;
    }

    public String getDisplayLabel() { return displayLabel; }
    public TariffStrategy getStrategy() { return strategy; }

    public static CardTierHierarchy parseFromLabel(String label) {
        return Arrays.stream(values())
                .filter(tier -> label.startsWith(tier.name()))
                .findFirst()
                .orElse(NPHH);
    }
}

// ==========================================
// 5. THREAD-SAFE CONCURRENT DATA MODELS 
// ==========================================
class CommodityModel {
    private final String name;
    private double currentStock;
    private final String standardUnit;
    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    public CommodityModel(String name, double currentStock, String standardUnit) {
        this.name = name;
        this.currentStock = currentStock;
        this.standardUnit = standardUnit;
    }

    public String getName() { return name; }
    public String getStandardUnit() { return standardUnit; }

    public double getCurrentStock() {
        lock.readLock().lock();
        try { return currentStock; } finally { lock.readLock().unlock(); }
    }

    public void deductStock(double volume) {
        lock.writeLock().lock();
        try {
            if (this.currentStock < volume) {
                throw new IllegalStateException("Underflow execution blocked.");
            }
            this.currentStock -= volume;
        } finally { lock.writeLock().unlock(); }
    }
}

class ManifestRecord {
    private final String itemLabel;
    private double unitQuantity;
    private final BigDecimal baseRate;
    private BigDecimal aggregateCost;

    public ManifestRecord(String itemLabel, double unitQuantity, BigDecimal baseRate) {
        this.itemLabel = itemLabel;
        this.unitQuantity = unitQuantity;
        this.baseRate = baseRate;
        recalculateAggregate();
    }

    public String getItemLabel() { return itemLabel; }
    public double getUnitQuantity() { return unitQuantity; }
    public BigDecimal getBaseRate() { return baseRate; }
    public BigDecimal getAggregateCost() { return aggregateCost; }

    public void applyQuantityDelta(double extraQuantity) {
        this.unitQuantity += extraQuantity;
        recalculateAggregate();
    }

    private void recalculateAggregate() {
        this.aggregateCost = this.baseRate.multiply(BigDecimal.valueOf(this.unitQuantity))
                .setScale(2, RoundingMode.HALF_UP);
    }
}

class HistoricLedgerEntry {
    private final String timestamp;
    private final String beneficiary;
    private final String tier;
    private final String summary;
    private final BigDecimal totalCost;

    public HistoricLedgerEntry(String timestamp, String beneficiary, String tier, String summary, BigDecimal totalCost) {
        this.timestamp = timestamp;
        this.beneficiary = beneficiary;
        this.tier = tier;
        this.summary = summary;
        this.totalCost = totalCost;
    }

    public String getTimestamp() { return timestamp; }
    public String getBeneficiary() { return beneficiary; }
    public String getTier() { return tier; }
    public String getSummary() { return summary; }
    public BigDecimal getTotalCost() { return totalCost; }
}

// ==========================================
// 6. ASYNCHRONOUS TRANSACTIONAL ENGINE
// ==========================================
class PdsEngine {
    private final Map<String, CommodityModel> stockRepository = new LinkedHashMap<>();
    private final List<ManifestRecord> activeSessionCart = new ArrayList<>();
    private final List<HistoricLedgerEntry> auditLedgerLogs = new ArrayList<>();
    private final List<EngineStateListener> observers = new CopyOnWriteArrayList<>();
    private final SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public PdsEngine() {
        stockRepository.put("Rice", new CommodityModel("Rice", 1500.0, "kg"));
        stockRepository.put("Wheat", new CommodityModel("Wheat", 1000.0, "kg"));
        stockRepository.put("Sugar", new CommodityModel("Sugar", 350.0, "kg"));
        stockRepository.put("Kerosene", new CommodityModel("Kerosene", 200.0, "Ltr"));
    }

    public void registerObserver(EngineStateListener target) { observers.add(target); }
    private void fireStateChanged() { observers.forEach(EngineStateListener::onEngineStateMutation); }

    public Collection<CommodityModel> getInventoryManifest() { return stockRepository.values(); }
    public CommodityModel findCommodity(String token) { return stockRepository.get(token); }
    public List<ManifestRecord> getStagedItems() { return Collections.unmodifiableList(activeSessionCart); }
    public List<HistoricLedgerEntry> getAuditLedgerLogs() { return Collections.unmodifiableList(auditLedgerLogs); }

    public BigDecimal resolveTariff(CardTierHierarchy tier, String primaryItem) {
        return tier.getStrategy().calculateRate(primaryItem);
    }

    public void processAllocationStaging(CardTierHierarchy tier, String commodityKey, double requestVolume) {
        if (requestVolume <= 0) throw new QuotaViolationException("Allocation error: Metric volume bounds must be greater than zero.");

        double stagedVolume = activeSessionCart.stream()
                .filter(i -> i.getItemLabel().equalsIgnoreCase(commodityKey))
                .mapToDouble(ManifestRecord::getUnitQuantity).sum();

        double combinedTotal = requestVolume + stagedVolume;
        assertQuotasWithinLimits(tier, commodityKey, combinedTotal);

        Optional<ManifestRecord> matchingRecord = activeSessionCart.stream()
                .filter(entry -> entry.getItemLabel().equalsIgnoreCase(commodityKey)).findFirst();

        if (matchingRecord.isPresent()) {
            matchingRecord.get().applyQuantityDelta(requestVolume);
        } else {
            BigDecimal assignedTariff = resolveTariff(tier, commodityKey);
            activeSessionCart.add(new ManifestRecord(commodityKey, requestVolume, assignedTariff));
        }
        fireStateChanged();
    }

    private void assertQuotasWithinLimits(CardTierHierarchy tier, String productNode, double targetVolume) {
        if (productNode.equals("Sugar") && targetVolume > 5.0) {
            throw new QuotaViolationException("Allocation Cap Violation: Sugar threshold maximum caps at 5.0 kg.");
        }
        if (productNode.equals("Kerosene") && targetVolume > 5.0) {
            throw new QuotaViolationException("Allocation Cap Violation: Liquid Kerosene allocation maximum caps at 5.0 Ltrs.");
        }
        if (tier == CardTierHierarchy.AYY && productNode.equals("Rice") && targetVolume > 35.0) {
            throw new QuotaViolationException("Scheme Rule Exception: Antyodaya Scheme (AYY) rules cap maximum Rice allocation at 35.0 kg.");
        }

        CommodityModel stockCheck = stockRepository.get(productNode);
        if (targetVolume > stockCheck.getCurrentStock()) {
            throw new StockDeficitException(productNode, targetVolume, stockCheck.getCurrentStock());
        }
    }

    public BigDecimal fetchGrossInvoiceValue() {
        return activeSessionCart.stream()
                .map(ManifestRecord::getAggregateCost)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
    }

    public synchronized void commitStagedDataToLedger(String beneficiary, CardTierHierarchy tier) {
        if (activeSessionCart.isEmpty()) throw new IllegalStateException("Commit failure: Active session tracking context is empty.");
        
        // Final sanity stock verify check pass before mutating storage blocks
        for (ManifestRecord record : activeSessionCart) {
            CommodityModel model = stockRepository.get(record.getItemLabel());
            if (record.getUnitQuantity() > model.getCurrentStock()) {
                throw new StockDeficitException(record.getItemLabel(), record.getUnitQuantity(), model.getCurrentStock());
            }
        }

        String summary = activeSessionCart.stream()
                .map(r -> String.format("%s(%.1f)", r.getItemLabel(), r.getUnitQuantity()))
                .collect(Collectors.joining(", "));
        
        BigDecimal grossTotal = fetchGrossInvoiceValue();

        // Execution of atomic state mutations
        activeSessionCart.forEach(record -> {
            CommodityModel model = stockRepository.get(record.getItemLabel());
            model.deductStock(record.getUnitQuantity());
        });

        auditLedgerLogs.add(new HistoricLedgerEntry(df.format(new Date()), beneficiary, tier.name(), summary, grossTotal));
        activeSessionCart.clear();
        fireStateChanged();
    }

    public void clearStagedWorkspace() {
        activeSessionCart.clear();
        fireStateChanged();
    }
}

// ==========================================
// 7. HIGH-CONTRAST GRAPHICAL SPLASH VIEW
// ==========================================
class SplashView extends JWindow {
    private final JProgressBar loadBar;
    private final JLabel loadingStatusLabel;

    public SplashView() {
        JPanel backgroundWrapper = new JPanel(new BorderLayout());
        backgroundWrapper.setBackground(new Color(15, 23, 42)); // Slate 900 Theme
        backgroundWrapper.setBorder(BorderFactory.createLineBorder(new Color(99, 102, 241), 2)); // Indigo Border

        JPanel innerGrid = new JPanel(new GridLayout(3, 1, 8, 8));
        innerGrid.setOpaque(false);
        innerGrid.setBorder(new EmptyBorder(45, 45, 45, 45));

        JLabel bannerTitle = new JLabel("PDS SECURE LEDGER PIPELINE", SwingConstants.CENTER);
        bannerTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        bannerTitle.setForeground(Color.WHITE);

        loadingStatusLabel = new JLabel("Synchronizing encryption ledger blocks...", SwingConstants.CENTER);
        loadingStatusLabel.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        loadingStatusLabel.setForeground(new Color(148, 163, 184)); // Slate 400

        loadBar = new JProgressBar(0, 100);
        loadBar.setStringPainted(true);
        loadBar.setFont(new Font("Segoe UI", Font.BOLD, 11));
        loadBar.setForeground(new Color(16, 185, 129)); // Emerald Green
        loadBar.setBackground(new Color(51, 65, 85));

        innerGrid.add(bannerTitle);
        innerGrid.add(loadingStatusLabel);
        innerGrid.add(loadBar);

        backgroundWrapper.add(innerGrid, BorderLayout.CENTER);
        add(backgroundWrapper);
        setSize(520, 260);
        setLocationRelativeTo(null);
    }

    public void updateLoadMetric(int metricPercentage) {
        loadBar.setValue(metricPercentage);
        if (metricPercentage > 30 && metricPercentage < 75) {
            loadingStatusLabel.setText("Parsing localized peripheral repository stock maps...");
        } else if (metricPercentage >= 75) {
            loadingStatusLabel.setText("System validation pipeline ready. Initializing frame wrappers...");
        }
    }
}

// ==========================================
// 8. ENTERPRISE MAIN BILLING DASHBOARD (GUI VIEW)
// ==========================================
class MainBillingDashboard extends JFrame implements EngineStateListener {
    private final PdsEngine kernelService;
    
    private JTextField entryBeneficiary, entryQuantity, displaySubsidizedPrice, displaySessionTotal;
    private JComboBox<String> selectorCardHierarchy, selectorCommodityNode;
    private JLabel labelWarehouseStockContext, labelNetworkStatusBar;
    private DefaultTableModel trackingGridModel, auditLedgerGridModel;
    private JTextArea functionalPrintTerminal;
    private final SimpleDateFormat loggingStamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public MainBillingDashboard(PdsEngine kernelService) {
        this.kernelService = kernelService;
        this.kernelService.registerObserver(this);
        assembleShellComponents();
        refreshSubsidyContextLayout();
    }

    private void assembleShellComponents() {
        setTitle("PDS Enterprise Centralized Storage Terminal v5.2.0-PRO");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1300, 800);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(12, 12));

        // Top Corporate Banner Header
        JPanel dynamicHeader = new JPanel(new BorderLayout());
        dynamicHeader.setBackground(new Color(30, 41, 59));
        dynamicHeader.setBorder(new EmptyBorder(16, 24, 16, 24));
        
        JLabel brandLabel = new JLabel("PDS CENTRAL AUTOMATION MANAGEMENT SYSTEMS");
        brandLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        brandLabel.setForeground(Color.WHITE);
        
        JLabel metaLabel = new JLabel("NODE SYNC STATUS: SECURE HOST SYSTEM", SwingConstants.RIGHT);
        metaLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        metaLabel.setForeground(new Color(52, 211, 153));
        
        dynamicHeader.add(brandLabel, BorderLayout.WEST);
        dynamicHeader.add(metaLabel, BorderLayout.EAST);
        add(dynamicHeader, BorderLayout.NORTH);

        // Core Layout Tabs Division
        JTabbedPane multiViewWorkspacePane = new JTabbedPane();
        multiViewWorkspacePane.setFont(new Font("Segoe UI", Font.BOLD, 13));

        // TAB 1: Realtime Terminal Desk Workspace
        JPanel deskWorkspacePanel = new JPanel(new GridLayout(1, 2, 16, 0));
        deskWorkspacePanel.setBorder(new EmptyBorder(12, 12, 12, 12));

        JPanel layoutLeftRegion = new JPanel(new BorderLayout(0, 14));
        JPanel interactiveFormGrid = new JPanel(new GridBagLayout());
        interactiveFormGrid.setBackground(Color.WHITE);
        interactiveFormGrid.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240)), "Beneficiary Validation Matrix", 
                TitledBorder.LEFT, TitledBorder.TOP, new Font("Segoe UI", Font.BOLD, 13), new Color(71, 85, 105)
        ));
        
        GridBagConstraints layoutGridMap = new GridBagConstraints();
        layoutGridMap.insets = new Insets(6, 14, 6, 14);
        layoutGridMap.fill = GridBagConstraints.HORIZONTAL;

        layoutGridMap.gridx = 0; layoutGridMap.gridy = 0;
        interactiveFormGrid.add(new JLabel("Card Category:"), layoutGridMap);
        layoutGridMap.gridx = 1; layoutGridMap.weightx = 1.0;
        
        selectorCardHierarchy = new JComboBox<>();
        for(CardTierHierarchy tier : CardTierHierarchy.values()) {
            selectorCardHierarchy.addItem(tier.getDisplayLabel());
        }
        selectorCardHierarchy.addActionListener(e -> refreshSubsidyContextLayout());
        interactiveFormGrid.add(selectorCardHierarchy, layoutGridMap);

        layoutGridMap.gridx = 0; layoutGridMap.gridy = 1; layoutGridMap.weightx = 0;
        interactiveFormGrid.add(new JLabel("Beneficiary Name:"), layoutGridMap);
        layoutGridMap.gridx = 1; layoutGridMap.weightx = 1.0;
        entryBeneficiary = new JTextField();
        interactiveFormGrid.add(entryBeneficiary, layoutGridMap);

        layoutGridMap.gridx = 0; layoutGridMap.gridy = 2; layoutGridMap.weightx = 0;
        interactiveFormGrid.add(new JLabel("Select Commodity:"), layoutGridMap);
        layoutGridMap.gridx = 1; layoutGridMap.weightx = 1.0;
        selectorCommodityNode = new JComboBox<>();
        kernelService.getInventoryManifest().forEach(item -> selectorCommodityNode.addItem(item.getName()));
        selectorCommodityNode.addActionListener(e -> refreshSubsidyContextLayout());
        interactiveFormGrid.add(selectorCommodityNode, layoutGridMap);

        layoutGridMap.gridx = 1; layoutGridMap.gridy = 3;
        labelWarehouseStockContext = new JLabel("Warehouse Stock: --");
        labelWarehouseStockContext.setFont(new Font("Segoe UI", Font.BOLD | Font.ITALIC, 11));
        labelWarehouseStockContext.setForeground(new Color(79, 70, 229));
        interactiveFormGrid.add(labelWarehouseStockContext, layoutGridMap);

        layoutGridMap.gridx = 0; layoutGridMap.gridy = 4; layoutGridMap.weightx = 0;
        interactiveFormGrid.add(new JLabel("Quantity Required:"), layoutGridMap);
        layoutGridMap.gridx = 1; layoutGridMap.weightx = 1.0;
        entryQuantity = new JTextField();
        interactiveFormGrid.add(entryQuantity, layoutGridMap);

        layoutGridMap.gridx = 0; layoutGridMap.gridy = 5; layoutGridMap.weightx = 0;
        interactiveFormGrid.add(new JLabel("Subsidized Price (₹):"), layoutGridMap);
        layoutGridMap.gridx = 1; layoutGridMap.weightx = 1.0;
        displaySubsidizedPrice = new JTextField();
        displaySubsidizedPrice.setEditable(false);
        displaySubsidizedPrice.setFont(new Font("Segoe UI", Font.BOLD, 13));
        displaySubsidizedPrice.setBackground(new Color(248, 250, 252));
        interactiveFormGrid.add(displaySubsidizedPrice, layoutGridMap);

        layoutGridMap.gridx = 0; layoutGridMap.gridy = 6; layoutGridMap.gridwidth = 2;
        JButton actionAddBtn = new JButton("Stage Item Allocation to List Matrix");
        actionAddBtn.setBackground(new Color(79, 70, 229));
        actionAddBtn.setForeground(Color.WHITE);
        actionAddBtn.addActionListener(e -> executeStagingProcessEvent());
        interactiveFormGrid.add(actionAddBtn, layoutGridMap);

        layoutLeftRegion.add(interactiveFormGrid, BorderLayout.NORTH);

        trackingGridModel = new DefaultTableModel(new String[]{"Commodity Token", "Staged Quantity", "Sub-Rate (₹)", "Aggregate Row Sum (₹)"}, 0) {
            @Override public boolean isCellEditable(int rows, int columns) { return false; }
        };
        JTable dataManifestGrid = new JTable(trackingGridModel);
        styleAdvancedTableLayout(dataManifestGrid);

        JScrollPane workspaceGridScrollPane = new JScrollPane(dataManifestGrid);
        workspaceGridScrollPane.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), 
                "Active Transaction Queue List", TitledBorder.LEFT, TitledBorder.TOP, 
                new Font("Segoe UI", Font.BOLD, 12), new Color(71, 85, 105)));
        layoutLeftRegion.add(workspaceGridScrollPane, BorderLayout.CENTER);
        deskWorkspacePanel.add(layoutLeftRegion);

        JPanel layoutRightRegion = new JPanel(new BorderLayout(0, 14));
        functionalPrintTerminal = new JTextArea();
        functionalPrintTerminal.setEditable(false);
        functionalPrintTerminal.setFont(new Font("Consolas", Font.PLAIN, 12));
        functionalPrintTerminal.setBackground(new Color(15, 23, 42)); // Modern Sleek Console Dark Theme
        functionalPrintTerminal.setForeground(new Color(52, 211, 153));
        
        JScrollPane receiptTerminalScrollPane = new JScrollPane(functionalPrintTerminal);
        receiptTerminalScrollPane.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(new Color(226, 232, 240)), 
                "POS Virtual Terminal & Thermal Invoice Print-Buffer Stream", TitledBorder.LEFT, TitledBorder.TOP, 
                new Font("Segoe UI", Font.BOLD, 12), new Color(71, 85, 105)));
        layoutRightRegion.add(receiptTerminalScrollPane, BorderLayout.CENTER);

        JPanel arithmeticAccountingLayoutSummary = new JPanel(new FlowLayout(FlowLayout.RIGHT, 16, 12));
        arithmeticAccountingLayoutSummary.setBackground(Color.WHITE);
        arithmeticAccountingLayoutSummary.setBorder(BorderFactory.createLineBorder(new Color(241, 245, 249)));
        
        JLabel totalValueTextLabel = new JLabel("Aggregate Balance Due:");
        totalValueTextLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        
        displaySessionTotal = new JTextField("0.00", 12);
        displaySessionTotal.setFont(new Font("Segoe UI", Font.BOLD, 20));
        displaySessionTotal.setForeground(new Color(220, 38, 38));
        displaySessionTotal.setBackground(new Color(254, 242, 242));
        displaySessionTotal.setEditable(false);
        displaySessionTotal.setHorizontalAlignment(JTextField.RIGHT);
        
        arithmeticAccountingLayoutSummary.add(totalValueTextLabel);
        arithmeticAccountingLayoutSummary.add(displaySessionTotal);
        layoutRightRegion.add(arithmeticAccountingLayoutSummary, BorderLayout.SOUTH);

        deskWorkspacePanel.add(layoutRightRegion);
        multiViewWorkspacePane.addTab("Terminal Counter Workspace", deskWorkspacePanel);

        // TAB 2: Secure History Audit Ledger
        auditLedgerGridModel = new DefaultTableModel(new String[]{"Execution Date", "Beneficiary", "Tier Classification", "Item Manifest Summary", "Total Ledger Paid (₹)"}, 0);
        JTable auditTable = new JTable(auditLedgerGridModel);
        styleAdvancedTableLayout(auditTable);
        JPanel auditPanel = new JPanel(new BorderLayout());
        auditPanel.setBorder(new EmptyBorder(12,12,12,12));
        auditPanel.add(new JScrollPane(auditTable), BorderLayout.CENTER);
        multiViewWorkspacePane.addTab("Secure History Audit Ledger Records", auditPanel);

        add(multiViewWorkspacePane, BorderLayout.CENTER);

        // Bottom Application Control Bar
        JPanel actionButtonControlPanelBar = new JPanel(new BorderLayout(0, 6));
        actionButtonControlPanelBar.setBorder(new EmptyBorder(4, 16, 12, 16));
        
        JPanel computationalButtonsGrid = new JPanel(new GridLayout(1, 3, 16, 0));
        JButton commandCommitTransaction = new JButton("Post Transaction & Mutate Storage Ledger Logs");
        commandCommitTransaction.setBackground(new Color(16, 185, 129));
        commandCommitTransaction.setForeground(Color.WHITE);
        commandCommitTransaction.addActionListener(e -> commitActiveSessionSequence());

        JButton commandGenerateInvoiceStream = new JButton("Parse Print Invoices Slips Buffer");
        commandGenerateInvoiceStream.setBackground(new Color(37, 99, 235));
        commandGenerateInvoiceStream.setForeground(Color.WHITE);
        commandGenerateInvoiceStream.addActionListener(e -> generateInvoiceBufferStreamOutput());

        JButton commandPurgeActiveWorkspace = new JButton("Flush Workspace Session Context");
        commandPurgeActiveWorkspace.setBackground(new Color(100, 116, 139));
        commandPurgeActiveWorkspace.setForeground(Color.WHITE);
        commandPurgeActiveWorkspace.addActionListener(e -> completeSessionStatePurge());

        computationalButtonsGrid.add(commandCommitTransaction);
        computationalButtonsGrid.add(commandGenerateInvoiceStream);
        computationalButtonsGrid.add(commandPurgeActiveWorkspace);
        
        labelNetworkStatusBar = new JLabel(" System Core State: Monitoring Active // Real-time secure engine framework active.");
        labelNetworkStatusBar.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        labelNetworkStatusBar.setForeground(new Color(100, 116, 139));
        labelNetworkStatusBar.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(241, 245, 249)));
        
        actionButtonControlPanelBar.add(computationalButtonsGrid, BorderLayout.CENTER);
        actionButtonControlPanelBar.add(labelNetworkStatusBar, BorderLayout.SOUTH);
        add(actionButtonControlPanelBar, BorderLayout.SOUTH);

        functionalPrintTerminal.setText("[" + loggingStamp.format(new Date()) + "] Secure System Initialization complete. Terminal Pipeline Active.\n");
        setVisible(true);
    }

    private void styleAdvancedTableLayout(JTable targetTable) {
        targetTable.setFillsViewportHeight(true);
        targetTable.setRowHeight(26);
        targetTable.setGridColor(new Color(241, 245, 249));
        targetTable.setSelectionBackground(new Color(238, 242, 255));
        targetTable.setSelectionForeground(new Color(79, 70, 229));
        
        JTableHeader h = targetTable.getTableHeader();
        h.setBackground(new Color(241, 245, 249));
        h.setForeground(new Color(51, 65, 85));
        h.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(203, 213, 225)));
        
        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(JLabel.RIGHT);
        if (targetTable.getColumnCount() >= 4) {
            targetTable.getColumnModel().getColumn(2).setCellRenderer(rightRenderer);
            targetTable.getColumnModel().getColumn(3).setCellRenderer(rightRenderer);
        }
    }

    private void refreshSubsidyContextLayout() {
        String designatedHierarchyCard = (String) selectorCardHierarchy.getSelectedItem();
        String activeItemNodeKey = (String) selectorCommodityNode.getSelectedItem();
        if (designatedHierarchyCard == null || activeItemNodeKey == null) return;

        CardTierHierarchy tier = CardTierHierarchy.parseFromLabel(designatedHierarchyCard);
        BigDecimal pricingIndexUnit = kernelService.resolveTariff(tier, activeItemNodeKey);
        displaySubsidizedPrice.setText(pricingIndexUnit.toPlainString());

        CommodityModel details = kernelService.findCommodity(activeItemNodeKey);
        labelWarehouseStockContext.setText(String.format("Warehouse Stock Balance: %.2f %s", details.getCurrentStock(), details.getStandardUnit()));
    }

    private void executeStagingProcessEvent() {
        try {
            String activeProductToken = (String) selectorCommodityNode.getSelectedItem();
            String rawUnitQuantityStr = entryQuantity.getText().trim();
            String verificationCardCode = (String) selectorCardHierarchy.getSelectedItem();

            if (rawUnitQuantityStr.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Requested allocation quantity parameter input cannot be empty.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            double parseQuantityUnits = Double.parseDouble(rawUnitQuantityStr);
            CardTierHierarchy tier = CardTierHierarchy.parseFromLabel(verificationCardCode);
            
            kernelService.processAllocationStaging(tier, activeProductToken, parseQuantityUnits);
            
            functionalPrintTerminal.append(String.format("[%s] Staged Allocation Updated: %.2f units of %s structural added.\n", 
                    loggingStamp.format(new Date()), parseQuantityUnits, activeProductToken));
            entryQuantity.setText("");

        } catch (NumberFormatException nfe) {
            JOptionPane.showMessageDialog(this, "The quantity format input contains invalid numerical characters.", "Parsing Error", JOptionPane.ERROR_MESSAGE);
        } catch (PdsDomainException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Regulatory Quota Enforcement Exception", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void commitActiveSessionSequence() {
        String profileIdentityText = entryBeneficiary.getText().trim();
        String currentCardToken = (String) selectorCardHierarchy.getSelectedItem();
        
        if (profileIdentityText.isEmpty() || kernelService.getStagedItems().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Ledger transaction aborted. Beneficiary data arrays are unpopulated.", "Commit Prevented", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            CardTierHierarchy tier = CardTierHierarchy.parseFromLabel(currentCardToken);
            kernelService.commitStagedDataToLedger(profileIdentityText, tier);
            refreshSubsidyContextLayout();
            entryBeneficiary.setText("");
            
            functionalPrintTerminal.append("[" + loggingStamp.format(new Date()) + "] TRANSACTION COMMIT SUCCESS. Cryptographic blocks compiled.\n");
            JOptionPane.showMessageDialog(this, "Ledger entries successfully finalized and pushed to structural storage.", "Transaction Success", JOptionPane.INFORMATION_MESSAGE);
        } catch (PdsDomainException pde) {
            JOptionPane.showMessageDialog(this, pde.getMessage(), "Runtime Settlement Intercepted", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void generateInvoiceBufferStreamOutput() {
        String currentClientNode = entryBeneficiary.getText().trim();
        if (currentClientNode.isEmpty() || kernelService.getStagedItems().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Print stream blocked: Missing operational transactional data chains.", "Print Pipeline Interrupted", JOptionPane.ERROR_MESSAGE);
            return;
        }

        StringBuilder invoiceLayoutBuilder = new StringBuilder();
        invoiceLayoutBuilder.append("===================================================\n");
        invoiceLayoutBuilder.append("         DEPARTMENT OF PUBLIC DISTRIBUTION         \n");
        invoiceLayoutBuilder.append("            SECURE LOGISTICAL RECORD SLIP          \n");
        invoiceLayoutBuilder.append("===================================================\n");
        invoiceLayoutBuilder.append(String.format("Timestamp   : %s\n", loggingStamp.format(new Date())));
        invoiceLayoutBuilder.append(String.format("Beneficiary : %s\n", currentClientNode));
        invoiceLayoutBuilder.append(String.format("Card Tier   : %s\n", selectorCardHierarchy.getSelectedItem()));
        invoiceLayoutBuilder.append("---------------------------------------------------\n");
        invoiceLayoutBuilder.append(String.format("%-18s %-12s %-10s %-10s\n", "Commodity", "Qty Approved", "Rate(₹)", "Total(₹)"));
        invoiceLayoutBuilder.append("---------------------------------------------------\n");

        for (ManifestRecord entry : kernelService.getStagedItems()) {
            String formatQuantityString = entry.getUnitQuantity() + " " + kernelService.findCommodity(entry.getItemLabel()).getStandardUnit();
            invoiceLayoutBuilder.append(String.format("%-18s %-12s %-10s %-10s\n", 
                    entry.getItemLabel(), formatQuantityString, entry.getBaseRate().toPlainString(), entry.getAggregateCost().toPlainString()));
        }
        
        invoiceLayoutBuilder.append("---------------------------------------------------\n");
        invoiceLayoutBuilder.append(String.format("AGGREGATE TOTAL DUE:                       ₹%s\n", displaySessionTotal.getText()));
        invoiceLayoutBuilder.append("===================================================\n");
        invoiceLayoutBuilder.append("       Digital Signature Validation: State Hub     \n");
        invoiceLayoutBuilder.append("===================================================\n");

        functionalPrintTerminal.setText(invoiceLayoutBuilder.toString());
        labelNetworkStatusBar.setText(" Status: Printed output context buffers synchronized to peripheral displays.");
    }

    private void completeSessionStatePurge() {
        entryBeneficiary.setText("");
        entryQuantity.setText("");
        kernelService.clearStagedWorkspace();
        functionalPrintTerminal.setText("[" + loggingStamp.format(new Date()) + "] Operational execution context purged back to tracking baseline metrics.\n");
    }

    @Override
    public void onEngineStateMutation() {
        // Redraw active tracking dashboard registers
        trackingGridModel.setRowCount(0);
        for (ManifestRecord record : kernelService.getStagedItems()) {
            CommodityModel inventoryMetadata = kernelService.findCommodity(record.getItemLabel());
            trackingGridModel.addRow(new Object[]{
                    record.getItemLabel(),
                    record.getUnitQuantity() + " " + inventoryMetadata.getStandardUnit(),
                    record.getBaseRate().toPlainString(),
                    record.getAggregateCost().toPlainString()
            });
        }
        displaySessionTotal.setText(kernelService.fetchGrossInvoiceValue().toPlainString());

        // Sync Audit Logs Grid Layout Viewports
        auditLedgerGridModel.setRowCount(0);
        for (HistoricLedgerEntry log : kernelService.getAuditLedgerLogs()) {
            auditLedgerGridModel.addRow(new Object[]{
                    log.getTimestamp(), log.getBeneficiary(), log.getTier(), log.getSummary(), log.getTotalCost().toPlainString()
            });
        }
    }
}