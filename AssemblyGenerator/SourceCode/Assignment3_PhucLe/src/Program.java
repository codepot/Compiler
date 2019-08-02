
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 *
 * @author Peter
 */
public class Program extends javax.swing.JFrame {

    File sourceCode = null;
    File output = null;
    SyntaxAnalyzer analyser = new SyntaxAnalyzer();
    AssemblyGenerator assemblyGenerator = new AssemblyGenerator();

    
    public Program() {
        initComponents();
        jSplitPane.setResizeWeight(0.5);
        this.setExtendedState(this.getExtendedState() | JFrame.MAXIMIZED_BOTH);
        
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        btn_SelectSourceCode = new javax.swing.JButton();
        lbl_notification = new javax.swing.JLabel();
        jSplitPane = new javax.swing.JSplitPane();
        jScrollPane1 = new javax.swing.JScrollPane();
        txt_source = new javax.swing.JTextArea();
        jScrollPane3 = new javax.swing.JScrollPane();
        txt_result = new javax.swing.JEditorPane();
        jSeparator2 = new javax.swing.JSeparator();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Symbol Table Handling & Assembly Code Generating, by student: Phuc Le, CPSC323");
        setBackground(java.awt.SystemColor.activeCaption);

        btn_SelectSourceCode.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        btn_SelectSourceCode.setForeground(new java.awt.Color(0, 102, 102));
        btn_SelectSourceCode.setText("Select Source Code File");
        btn_SelectSourceCode.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_SelectSourceCodeActionPerformed(evt);
            }
        });

        lbl_notification.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        lbl_notification.setForeground(new java.awt.Color(0, 153, 51));
        lbl_notification.setText("please select source code file (*.txt only)");

        txt_source.setColumns(40);
        txt_source.setFont(new java.awt.Font("Monospaced", 0, 24)); // NOI18N
        txt_source.setRows(5);
        jScrollPane1.setViewportView(txt_source);

        jSplitPane.setLeftComponent(jScrollPane1);

        txt_result.setFont(new java.awt.Font("Consolas", 1, 24)); // NOI18N
        jScrollPane3.setViewportView(txt_result);

        jSplitPane.setRightComponent(jScrollPane3);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(btn_SelectSourceCode)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lbl_notification, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addComponent(jSplitPane, javax.swing.GroupLayout.DEFAULT_SIZE, 1495, Short.MAX_VALUE)
            .addComponent(jSeparator2)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btn_SelectSourceCode, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lbl_notification, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSplitPane, javax.swing.GroupLayout.DEFAULT_SIZE, 845, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator2, javax.swing.GroupLayout.PREFERRED_SIZE, 5, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        btn_SelectSourceCode.getAccessibleContext().setAccessibleName("btn_selectSourceCode");

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btn_SelectSourceCodeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_SelectSourceCodeActionPerformed
        JFileChooser fileChooser = new JFileChooser();
        FileNameExtensionFilter filter = new FileNameExtensionFilter("TEXT FILES", "txt", "text");
        fileChooser.setFileFilter(filter);
        fileChooser.setCurrentDirectory(new File("."));
        fileChooser.showOpenDialog(this);
        sourceCode = fileChooser.getSelectedFile();
        if (sourceCode != null) {
            // txt_result.setText("");
            analyser.reset();
            output = new File(sourceCode.getAbsolutePath().replace(".txt", "_output.txt"));
            if (sourceCode != null) {
                List<String> fileContent = readFile(sourceCode);
                analyser.clearTokens();
                String source = "";
                int lineNumber = 1;
                for (String line : fileContent) {
                    source += line + "\n";
                    analyser.input(line, lineNumber);
                    assemblyGenerator.input(line, lineNumber);
                    lineNumber++;
                }
                txt_source.setText(source);
                analyser.func_rat16f();
                String result = analyser.getResult();                
                
                 if (!result.contains("THE SYNTAX IS CORRECT")) {                
                     txt_result.setText(analyser.getErrorInfo());
                } 
                
                else {
                    assemblyGenerator.func_rat16f();
                    String error_msg = assemblyGenerator.getErrorMessage();
                    String output = "";
                    if (error_msg.equals("")) {
                        for (Symbol s : assemblyGenerator.getSymbols()) {
                            output += s+"\n";
                        }
                        output += "\n----------------------------\n\n";
                        for (Instruction instruction : assemblyGenerator.getInstructions()) {
                            output += instruction+"\n";
                        }
                    } else {
                        for (Symbol s : assemblyGenerator.getSymbols()) {
                            output += s+"\n";
                        }
                        output +=  error_msg;
                    }
                    txt_result.setText(output);

                }
                System.out.println(assemblyGenerator.getResult());
                save(txt_result.getText());
                lbl_notification.setText("Output was saved in " + output.getAbsolutePath());
            }
        }
        analyser.reset();
        assemblyGenerator = new AssemblyGenerator();
    }//GEN-LAST:event_btn_SelectSourceCodeActionPerformed

    private void save(String tokens) {
        BufferedWriter bufferWriter = null;
        try {
            bufferWriter = new BufferedWriter(new FileWriter(output));

            txt_result.write(bufferWriter);

        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            if (bufferWriter != null) {
                try {
                    bufferWriter.close();
                } catch (IOException e) {
                }
            }
        }
    }

    public java.util.List<String> readFile(File fin) {
        FileInputStream fis;
        List<String> fileContent = new ArrayList<>();

        try {
            fis = new FileInputStream(fin);
            BufferedReader br = new BufferedReader(new InputStreamReader(fis));
            String line = null;
            while ((line = br.readLine()) != null) {
                fileContent.add(line);
            }
            br.close();
        } catch (FileNotFoundException ex) {
            System.err.println("File Not Found");
        } catch (IOException ex) {
            System.err.println("I/O Exception");
        }
        return fileContent;
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
       
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new Program().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btn_SelectSourceCode;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JSplitPane jSplitPane;
    private javax.swing.JLabel lbl_notification;
    private javax.swing.JEditorPane txt_result;
    private javax.swing.JTextArea txt_source;
    // End of variables declaration//GEN-END:variables
}
