package org.unindented.xslttester;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;

import javax.swing.BorderFactory;
import javax.swing.JApplet;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamSource;

/**
 * Simple XSLT tester applet.
 *
 * It's composed of three panels:
 * <ol>
 * <li>The left panel, where the user enters the XML ({@link XmlPanel}).</li>
 * <li>The right panel, where the user enters the XSLT ({@link XmlPanel}).</li>
 * <li>The bottom panel, where the result of the transformation is displayed ({@link ResultPanel}).</li>
 * </ol>
 *
 * @author Daniel Perez Alvarez
 */
public class XsltTester extends JApplet
{
    private static final long serialVersionUID = 1L;

    private final Font font = new Font(Font.MONOSPACED, Font.PLAIN, 14);

    private XmlPanel xmlPanel;
    private XmlPanel xsltPanel;
    private ResultPanel resultPanel;

    public String getAppletInfo()
    {
        return "Title: XsltTester v1.0, 28 Sep 2009\n" //
            + "Author: Daniel Perez Alvarez\n" //
            + "XSLT tester.";
    }

    public void init()
    {
        try
        {
            SwingUtilities.invokeAndWait(new Runnable()
            {
                public void run()
                {
                    initLAF();
                    createGUI();
                }
            });
        }
        catch (Exception e)
        {
            System.err.println("Couldn't create applet.");
            e.printStackTrace();
        }
    }

    private void initLAF()
    {
        try
        {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        }
        catch (Exception e)
        {
            System.err.println("Couldn't set look & feel.");
            e.printStackTrace();
        }
    }

    private void createGUI()
    {
        xmlPanel = new XmlPanel("Your XML");
        xsltPanel = new XmlPanel("Your XSLT");
        resultPanel = new ResultPanel("Result");

        JSplitPane horizontalSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, xmlPanel, xsltPanel);
        horizontalSplit.setResizeWeight(0.5);
        horizontalSplit.setOneTouchExpandable(true);
        horizontalSplit.setBorder(BorderFactory.createEmptyBorder());

        JSplitPane verticalSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, horizontalSplit, resultPanel);
        verticalSplit.setResizeWeight(0.5);
        verticalSplit.setOneTouchExpandable(true);
        verticalSplit.setBorder(BorderFactory.createEmptyBorder());

        getContentPane().add(verticalSplit, BorderLayout.CENTER);

        // load sample data
        InputStream sampleXml = getClass().getResourceAsStream("/sample.xml");
        InputStream sampleXslt = getClass().getResourceAsStream("/sample.xsl");
        xmlPanel.setXml(MiscUtils.convertStreamToString(sampleXml));
        xsltPanel.setXml(MiscUtils.convertStreamToString(sampleXslt));
    }

    public static void main(final String[] args)
    {
        SwingUtilities.invokeLater(new Runnable()
        {
            public void run()
            {
                XsltTester applet = new XsltTester();
                applet.initLAF();
                applet.createGUI();

                JFrame frame = new JFrame("XSLT Tester");
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.getContentPane().add(applet);

                frame.pack();
                frame.setVisible(true);
            }
        });
    }

    /**
     * Panel where the user enters the XML.
     */
    public class XmlPanel extends JPanel
    {
        private static final long serialVersionUID = 1L;

        private final JTextArea xmlField;
        private final JButton prettifyButton;

        public XmlPanel(final String title)
        {
            super(new BorderLayout());

            // XML
            xmlField = new JTextArea();
            xmlField.setFont(font);
            xmlField.setLineWrap(false);
            // prettify
            prettifyButton = new JButton();
            prettifyButton.addActionListener(new ActionListener()
            {
                public void actionPerformed(final ActionEvent evt)
                {
                    prettify();
                }
            });

            add(new JScrollPane(xmlField), BorderLayout.CENTER);
            add(prettifyButton, BorderLayout.SOUTH);

            setBorder( //
                BorderFactory.createCompoundBorder( //
                    BorderFactory.createCompoundBorder( //
                        BorderFactory.createTitledBorder(title), //
                        BorderFactory.createEmptyBorder(5, 5, 5, 5) //
                    ), //
                    getBorder() //
                ) //
            );

            setAvailable(true);
        }

        public String getXml()
        {
            return xmlField.getText();
        }

        public void setXml(final String xml)
        {
            xmlField.setText(xml);
            xmlField.setCaretPosition(0);
        }

        public void setAvailable(final boolean available)
        {
            xmlField.setEnabled(available);
            prettifyButton.setEnabled(available);
            prettifyButton.setText(available ? "Prettify" : "Working...");
        }

        public void prettify()
        {
            setAvailable(false);

            SwingWorker worker = new SwingWorker()
            {
                public Object construct()
                {
                    return MiscUtils.prettify(getXml());
                }

                public void finished()
                {
                    setXml((String) get());
                    setAvailable(true);
                }
            };
            worker.start();
        }
    }

    /**
     * Panel where the result of the transformation is displayed.
     */
    public class ResultPanel extends JPanel
    {
        private static final long serialVersionUID = 1L;

        private final JTextArea resultField;
        private final JButton transformButton;

        public ResultPanel(final String title)
        {
            super(new BorderLayout());

            // result
            resultField = new JTextArea();
            resultField.setFont(font);
            resultField.setLineWrap(false);
            resultField.setEditable(false);
            // update
            transformButton = new JButton("Transform");
            transformButton.addActionListener(new ActionListener()
            {
                public void actionPerformed(final ActionEvent evt)
                {
                    ResultPanel.this.transform();
                }
            });

            add(new JScrollPane(resultField), BorderLayout.CENTER);
            add(transformButton, BorderLayout.SOUTH);

            setBorder( //
                BorderFactory.createCompoundBorder( //
                    BorderFactory.createCompoundBorder( //
                        BorderFactory.createTitledBorder(title), //
                        BorderFactory.createEmptyBorder(5, 5, 5, 5) //
                    ), //
                    getBorder() //
                ) //
            );

            setAvailable(true);
        }

        public String getResult()
        {
            return resultField.getText();
        }

        public void setResult(final String result)
        {
            resultField.setText(result);
            resultField.setCaretPosition(0);
        }

        public void setAvailable(final boolean available)
        {
            resultField.setEnabled(available);
            transformButton.setEnabled(available);
            transformButton.setText(available ? "Transform" : "Working...");
        }

        public void transform()
        {
            setAvailable(false);

            SwingWorker worker = new SwingWorker()
            {
                public Object construct()
                {
                    StreamSource xml = new StreamSource(new StringReader(xmlPanel.getXml()));
                    StreamSource xslt = new StreamSource(new StringReader(xsltPanel.getXml()));

                    Writer err = null;
                    try
                    {
                        err = new StringWriter();
                        return MiscUtils.transform(xml, xslt, null, err);
                    }
                    catch (TransformerException te)
                    {
                        if (err != null)
                        {
                            try
                            {
                                err.flush();
                                return err.toString();
                            }
                            catch (IOException e)
                            {
                                e.printStackTrace();
                            }
                        }
                    }

                    return null;
                }

                public void finished()
                {
                    setResult((String) get());
                    setAvailable(true);
                }
            };
            worker.start();
        }
    }
}
