package view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.EventQueue;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

import org.jdatepicker.DateModel;
import org.jdatepicker.impl.JDatePanelImpl;
import org.jdatepicker.impl.JDatePickerImpl;
import org.jdatepicker.impl.UtilDateModel;

import com.google.api.services.customsearch.model.Result;

import model.JulianDate;
import model.RestAPI;
import model.SaveResults;
import model.SearchClient;

import java.awt.Font;
import java.awt.Toolkit;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import javax.swing.UIManager;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.text.SimpleDateFormat;
import java.awt.event.ActionEvent;

@SuppressWarnings("serial")
public class MainApp extends JFrame {

	private JPanel contentPane;
	private List<Result> resultList;
	private List<String> nyResultList;
	private JEditorPane jtaResult;
	private JLabel jlHitsNum;
	private JLabel jlPageNum;
	private long pageNum;
	private long page;
	private JButton jbPre;
	private JButton jbNext;
	private JButton jbSave;
	private JButton jbSaveAll;
	private JLabel jlYahooMB;
	private int startingPage = 0;
	private int startingArticle = 0;
	private JDatePanelImpl startDatePanel;
	private JDatePickerImpl startDatePicker;
	private JDatePanelImpl endDatePanel;
	private JDatePickerImpl endDatePicker;

	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					MainApp frame = new MainApp();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	public MainApp() {
		//Main Form
		setTitle("Company News and Message Boards Data Crawler");
		setResizable(false);
//		setUndecorated(true);
//		setSize(700, 530);
//		setBounds(100, 100, 709, 523);
		setSize(1366, 740);
		setBounds(0, 0, 1366, 740);
//		setExtendedState(java.awt.Frame.MAXIMIZED_BOTH);
//		setVisible(true);
//	    setResizable(false);
	    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		contentPane = new JPanel();
		contentPane.setBackground(new Color(173, 216, 230));
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new BorderLayout(0, 0));
		setContentPane(contentPane);
		getContentPane().setLayout(null);
		try {
			ClassLoader cl = this.getClass().getClassLoader();
			ImageIcon programIcon = new ImageIcon(cl.getResource("icon.png"));
			setIconImage(programIcon.getImage());
		} catch (Exception whoJackedMyIcon) {
			System.out.println("Could not load program icon.");
		}
		
		//Search Key Label
		JLabel jlSearchKey = new JLabel("Search Key: ");
		jlSearchKey.setFont(new Font("Tahoma", Font.BOLD, 12));
		getContentPane().add(jlSearchKey, BorderLayout.WEST);
		jlSearchKey.setBounds(10, 40, 80, 20);
		
		//Search Textbox
		JTextField jtSearchKey = new JTextField();
		jtSearchKey.setFont(new Font("Tahoma", Font.PLAIN, 11));
		getContentPane().add(jtSearchKey, BorderLayout.WEST);
		jtSearchKey.setBounds(110, 40, 500, 25);
		jtSearchKey.setEnabled(true);
		
		//Source Label
		JLabel jlSrc = new JLabel("Source: ");
		jlSrc.setFont(new Font("Tahoma", Font.BOLD, 12));
		getContentPane().add(jlSrc, BorderLayout.WEST);
		jlSrc.setBounds(35, 8, 60, 20);
		
		//Source ComboBox
		String[] srcStrings = { "", "Newyork Times", "Washington Post", "Yahoo! Message Boards"};
		@SuppressWarnings({ "rawtypes", "unchecked" })
		JComboBox jcbSrc = new JComboBox(srcStrings);
		jcbSrc.setEditable(false);
		jcbSrc.setBackground(UIManager.getColor("ComboBox.disabledBackground"));
		jcbSrc.setFont(new Font("Tahoma", Font.PLAIN, 11));
		getContentPane().add(jcbSrc, BorderLayout.WEST);
		jcbSrc.setBounds(90, 8, 300, 25);
		jcbSrc.setSelectedItem(srcStrings[1]);
		jcbSrc.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
					if (jcbSrc.getSelectedIndex() == 3) {
						jtSearchKey.setText(null);
						jlYahooMB.setVisible(true);
						jlSearchKey.setVisible(false);
					} else {
						jlYahooMB.setVisible(false);
						jlSearchKey.setVisible(true);
					}
			}
		});
		
		//Blogs Inclusion
		JCheckBox jcBlogExc = new JCheckBox("Save Blogs Too");
		jcBlogExc.setFont(new Font("Tahoma", Font.BOLD, 12));
		getContentPane().add(jcBlogExc, BorderLayout.WEST);
		jcBlogExc.setBounds(490, 675, 135, 25);
		jcBlogExc.setSelected(true);
		jcBlogExc.setBackground(getForeground());
		
		//Number of Keywords Assertion Textbox
		JTextField jtNrKeywords = new JTextField();
		jtNrKeywords.setFont(new Font("Tahoma", Font.BOLD, 12));
		jtNrKeywords.setHorizontalAlignment(getX()/2);
		getContentPane().add(jtNrKeywords, BorderLayout.WEST);
		jtNrKeywords.setBounds(1030, 675, 30, 25);
		jtNrKeywords.setEnabled(false);
		jtNrKeywords.setText(null);
		jtNrKeywords.addKeyListener(new KeyAdapter() {
			public void keyTyped(KeyEvent e) {
			      char c = e.getKeyChar();
			      if ( ((c < '0') || (c > '9')) && (c != KeyEvent.VK_BACK_SPACE)) {
			         e.consume();  // ignore event
			      }
			   }
		});
		
		//Number of Keywords Assertion
		JCheckBox jcNrKeywords = new JCheckBox("Save Only Articles With More Than This Number of Keywords:");
		jcNrKeywords.setFont(new Font("Tahoma", Font.BOLD, 12));
		getContentPane().add(jcNrKeywords, BorderLayout.WEST);
		jcNrKeywords.setBounds(620, 675, 410, 25);
		jcNrKeywords.setSelected(false);
		jcNrKeywords.setBackground(getForeground());
		jcNrKeywords.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (jcNrKeywords.isSelected()) {
					jtNrKeywords.setEnabled(true);
				} else {
					jtNrKeywords.setText(null);
					jtNrKeywords.setEnabled(false);
				}
			}
		});
		
		//Yahoo Message Board
		jlYahooMB = new JLabel("Message Board: ");
		jlYahooMB.setFont(new Font("Tahoma", Font.BOLD, 12));
		getContentPane().add(jlYahooMB, BorderLayout.WEST);
		jlYahooMB.setBounds(10, 40, 150, 20);
		jlYahooMB.setVisible(false);
		
		//Start Date Label
		JLabel jlStartDate = new JLabel("Start Date: ");
		jlStartDate.setFont(new Font("Tahoma", Font.BOLD, 12));
		getContentPane().add(jlStartDate, BorderLayout.WEST);
		jlStartDate.setBounds(690, 40, 80, 20);
		
		//Start Date DatePicker
		UtilDateModel startModel = new UtilDateModel();
		startDatePanel = new JDatePanelImpl(startModel, new Properties());
		startDatePicker = new JDatePickerImpl(startDatePanel, new DateLabelFormatter());
		startDatePicker.setFont(new Font("Tahoma", Font.BOLD, 12));
		getContentPane().add(startDatePicker, BorderLayout.WEST);
		startDatePicker.setBounds(770, 40, 160, 26);

		//End Date Label
		JLabel jlEndDate = new JLabel("End Date: ");
		jlEndDate.setFont(new Font("Tahoma", Font.BOLD, 12));
		getContentPane().add(jlEndDate, BorderLayout.WEST);
		jlEndDate.setBounds(980, 40, 70, 20);
		
		//End Date DatePicker
		UtilDateModel endModel = new UtilDateModel();
		Calendar today = Calendar.getInstance();
		endModel.setDate(today.get(Calendar.YEAR), today.get(Calendar.MONTH), today.get(Calendar.DAY_OF_MONTH));
		endModel.setSelected(true);
		endDatePanel = new JDatePanelImpl(endModel, new Properties());
		endDatePicker = new JDatePickerImpl(endDatePanel, new DateLabelFormatter());
		endDatePicker.setFont(new Font("Tahoma", Font.BOLD, 12));
		getContentPane().add(endDatePicker, BorderLayout.WEST);
		endDatePicker.setBounds(1050, 40, 160, 26);
		
		//Number of Hits Label
		JLabel jlHitsNumTxt = new JLabel("Number of results: ");
		jlHitsNumTxt.setFont(new Font("Tahoma", Font.BOLD, 10));
		getContentPane().add(jlHitsNumTxt, BorderLayout.WEST);
		jlHitsNumTxt.setBounds(12, 90, 100, 20);
		
		//Number of Hits Label
		jlHitsNum = new JLabel("0");
		jlHitsNum.setFont(new Font("Tahoma", Font.BOLD, 10));
		getContentPane().add(jlHitsNum, BorderLayout.WEST);
		jlHitsNum.setBounds(130, 90, 50, 20);
		
		//Page Number Label
		JLabel jlPageNumTxt = new JLabel("Page: ");
		jlPageNumTxt.setFont(new Font("Tahoma", Font.BOLD, 10));
		getContentPane().add(jlPageNumTxt, BorderLayout.WEST);
		jlPageNumTxt.setBounds(200, 90, 50, 20);

		//Page Number
		jlPageNum = new JLabel("1");
		jlPageNum.setFont(new Font("Tahoma", Font.BOLD, 10));
		getContentPane().add(jlPageNum, BorderLayout.WEST);
		jlPageNum.setBounds(260, 90, 50, 20);
		
		//Search Result Text Area
		jtaResult = new JEditorPane();
//		jtaResult.setContentType("text/html");
//		jtaResult.setEditorKit(new HTMLEditorKit());
		jtaResult.setForeground(Color.BLACK);
		jtaResult.setEditable(false);
		jtaResult.setFont(new Font("Tahoma", Font.BOLD, 12));
		jtaResult.setBounds(5, 120, 1350, 545);
		jtaResult.addHyperlinkListener(new HyperlinkListener() {
	        @Override
	        public void hyperlinkUpdate(HyperlinkEvent e) {
	            if (e.getEventType().equals(HyperlinkEvent.EventType.ACTIVATED)) {
	                Desktop desktop = Desktop.getDesktop();
                    try {
                        desktop.browse(e.getURL().toURI());
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
	            }
	        }
	    });
		JScrollPane jsp = new JScrollPane(jtaResult, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		jsp.setViewportBorder(new LineBorder(new Color(0, 0, 0)));
		jsp.setBounds(5, 120, 1350, 545);
		getContentPane().add(jsp, BorderLayout.WEST);
		
		//Search Button
		JButton jbSearch = new JButton("Search");
		jbSearch.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (jcbSrc.getSelectedIndex() != 1) {
					jtaResult.setText("");
					pageNum = 1;
					resultList = search("\"" + jtSearchKey.getText() + "\"", jcbSrc.getSelectedIndex(), startDatePicker.getModel().getValue(),
							endDatePicker.getModel().getValue(), pageNum);
					jlPageNum.setText("");
					if (resultList != null && resultList.size() > 0) {
						for(Result result: resultList){
							jtaResult.setText(jtaResult.getText() + result.getLink() + "\n");
							jtaResult.setText(jtaResult.getText() + result.getSnippet() + "\n");
							jtaResult.setText(jtaResult.getText() + "\n");
							jlHitsNum.setText(nyResultList.get(0));
						}
					}
					jbPre.setEnabled(false);
					jbNext.setEnabled(true);
					jbSave.setEnabled(true);
					jbSaveAll.setEnabled(true);
				} else {
//					jtaResult.setText("");
//					page = 0;
//					try {
//						getContentPane().setCursor(new Cursor(Cursor.WAIT_CURSOR));
//						String k[] = jtSearchKey.getText().split("\\s+");
//						String searchKey = k[0];
//						for (int i = 1; i < k.length; i++) {
//							searchKey += "+" + k[i];
//						}
//						nyResultList = searchNYT(searchKey, startDatePicker.getModel(), endDatePicker.getModel(), page);
//						jlHitsNum.setText(getHitsNum(searchKey, startDatePicker.getModel(), endDatePicker.getModel()));
//						jlPageNum.setText(String.valueOf(page+1));
//						for(int i = 0; i < nyResultList.size(); i = i+7){
//							jtaResult.setText(jtaResult.getText() + nyResultList.get(i) + "\n");
//							jtaResult.setText(jtaResult.getText() + nyResultList.get(i+1) + "\n");
//							jtaResult.setText(jtaResult.getText() + nyResultList.get(i+2) + "\n");
//							jtaResult.setText(jtaResult.getText() + "\n");
//						}
//						if (nyResultList != null && nyResultList.size() > 0) {
//							jbPre.setEnabled(false);
//							jbNext.setEnabled(true);
//							jbSave.setEnabled(true);
//							jbSaveAll.setEnabled(true);
//						}
//					} catch (Exception e1) {
//						e1.printStackTrace();
//					}
//					getContentPane().setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
					
					searchInNYT(jtSearchKey.getText());
				}
			}
		});
		jbSearch.setFont(new Font("Tahoma", Font.BOLD, 14));
		getContentPane().add(jbSearch, BorderLayout.WEST);
		jbSearch.setBounds(1220, 80, 135, 30);
		
		//Previous Button
		jbPre = new JButton("Previous Page");
		jbPre.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (jcbSrc.getSelectedIndex() != 1) {
					jtaResult.setText("");
					pageNum -= 10;
					resultList = search("\"" + jtSearchKey.getText() + "\"", jcbSrc.getSelectedIndex(), startDatePicker.getModel().getValue(),
							endDatePicker.getModel().getValue(), pageNum);
					if (resultList != null && resultList.size() > 0) {
						for(Result result: resultList){
							jtaResult.setText(jtaResult.getText() + result.getLink() + "\n");
							jtaResult.setText(jtaResult.getText() + result.getSnippet() + "\n");
							jtaResult.setText(jtaResult.getText() + "\n");
						}
					}
					if (pageNum == 1) {
						jbPre.setEnabled(false);
					}
				} else {
					jtaResult.setText("");
					page--;
					try {
						String k[] = jtSearchKey.getText().split("\\s+");
						String searchKey = k[0];
						for (int i = 1; i < k.length; i++) {
							searchKey += "+" + k[i];
						}
						nyResultList = searchNYT(searchKey, startDatePicker.getModel(), endDatePicker.getModel(), page);
						jlPageNum.setText(String.valueOf(page+1));
						for(int i = 0; i < nyResultList.size(); i = i+7){
							jtaResult.setText(jtaResult.getText() + nyResultList.get(i) + "\n");
							jtaResult.setText(jtaResult.getText() + nyResultList.get(i+1) + "\n");
							jtaResult.setText(jtaResult.getText() + nyResultList.get(i+2) + "\n");
							jtaResult.setText(jtaResult.getText() + "\n");
						}
						jbNext.setEnabled(true);
						if (page == 0) {
							jbPre.setEnabled(false);
						}
					} catch (Exception e1) {
						e1.printStackTrace();
					}
				}
			}
		});
		jbPre.setFont(new Font("Tahoma", Font.BOLD, 12));
		getContentPane().add(jbPre, BorderLayout.WEST);
		jbPre.setBounds(5, 675, 120, 25);
		jbPre.setEnabled(false);
		
		//Next Button
		jbNext = new JButton("Next Page");
		jbNext.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (jcbSrc.getSelectedIndex() != 1) {
					jtaResult.setText("");
					pageNum += 10;
					resultList = search("\"" + jtSearchKey.getText() + "\"", jcbSrc.getSelectedIndex(), startDatePicker.getModel().getValue(),
							endDatePicker.getModel().getValue(), pageNum);
					if (resultList != null && resultList.size() > 0) {
						for(Result result: resultList){
//							URL url = null;
//							try {
//								url = new URL(result.getLink());
//							} catch (MalformedURLException e1) {
//								e1.printStackTrace();
//							}
//							jtaResult.setText(jtaResult.getText() + "<a href=\"" + url + "\">"+ url + "</a> " + "\n");
							jtaResult.setText(jtaResult.getText() + result.getLink() + "\n");
							jtaResult.setText(jtaResult.getText() + result.getSnippet() + "\n");
							jtaResult.setText(jtaResult.getText() + "\n");
						}
						jbPre.setEnabled(true);
					}
				} else {
					if (Long.parseLong(jlPageNum.getText()) <= 100) {
						jtaResult.setText("");
						page++;
						try {
							String k[] = jtSearchKey.getText().split("\\s+");
							String searchKey = k[0];
							for (int i = 1; i < k.length; i++) {
								searchKey += "+" + k[i];
							}
							nyResultList = searchNYT(searchKey, startDatePicker.getModel(), endDatePicker.getModel(), page);
							jlPageNum.setText(String.valueOf(page+1));
							if (nyResultList != null && nyResultList.size() > 0) {
								for(int i = 0; i < nyResultList.size(); i = i+7){
									jtaResult.setText(jtaResult.getText() + nyResultList.get(i) + "\n");
									jtaResult.setText(jtaResult.getText() + nyResultList.get(i+1) + "\n");
									jtaResult.setText(jtaResult.getText() + nyResultList.get(i+2) + "\n");
									jtaResult.setText(jtaResult.getText() + "\n");
								}
								jbPre.setEnabled(true);
							} else if (nyResultList == null || nyResultList.size() == 0) {
								jbNext.setEnabled(false);
							}
						} catch (Exception e1) {
							e1.printStackTrace();
						}
					} else {
						JOptionPane.showMessageDialog(null, "Showing the results beyond page 100 is not possible.", "Message", JOptionPane.INFORMATION_MESSAGE);
						jbPre.requestFocus();
					}		
				}	
			}
		});
		jbNext.setFont(new Font("Tahoma", Font.BOLD, 12));
		getContentPane().add(jbNext, BorderLayout.WEST);
		jbNext.setBounds(130, 675, 135, 25);
		jbNext.setEnabled(false);
		
		//Save Button
		jbSave = new JButton("Save Results");
		jbSave.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JFileChooser fc = new JFileChooser();
				fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				if (fc.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
					String dest = fc.getSelectedFile().toString();
					System.out.println(jtNrKeywords.getText());
					int keywordRepeatNr = (!jtNrKeywords.getText().equals("")) ? Integer.parseInt(jtNrKeywords.getText()): 0;
					if (jcbSrc.getSelectedIndex() != 1) {
						if (resultList != null && resultList.size() > 0) {
							SaveResults sr = new SaveResults();
							for(Result result: resultList){
								try {
									sr.saveToFile(result.getLink(), jtSearchKey.getText(), jcbSrc.getSelectedIndex(), null, dest,
											null, null, null, null, jcBlogExc.isSelected(), keywordRepeatNr);
								} catch (Exception e1) {
									e1.printStackTrace();
									JOptionPane.showMessageDialog(null, "Due to the network problem the saving process is suspended.",
											"Error", JOptionPane.ERROR_MESSAGE);
								}
							}
						}
					} else {
						if (nyResultList != null && nyResultList.size() > 0) {
							getContentPane().setCursor(new Cursor(Cursor.WAIT_CURSOR));
//							actionPerformed(e);
							SaveResults sr = new SaveResults();
							int numberOfAllResults = nyResultList.size();
							for(int i = 0; i < numberOfAllResults; i = i+7){
								try {
									sr.saveToFile(nyResultList.get(i), jtSearchKey.getText(), jcbSrc.getSelectedIndex(), nyResultList.get(i+1),
											dest, nyResultList.get(i+3), nyResultList.get(i+4), nyResultList.get(i+5), nyResultList.get(i+6),
											jcBlogExc.isSelected(), keywordRepeatNr);
								} catch (Exception e1) {
									e1.printStackTrace();
									JOptionPane.showMessageDialog(null, "Due to the network problem the saving process is suspended.",
											"Error", JOptionPane.ERROR_MESSAGE);
								}
							}
							getContentPane().setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
							Toolkit.getDefaultToolkit().beep();
							JOptionPane.showMessageDialog(null, "Finished!", "Message", JOptionPane.INFORMATION_MESSAGE);
						}
					}
				}
			}
		});
		jbSave.setFont(new Font("Tahoma", Font.BOLD, 12));
		getContentPane().add(jbSave, BorderLayout.WEST);
		jbSave.setBounds(1080, 675, 135, 25);
		jbSave.setEnabled(false);
		
		//Save All Button
		jbSaveAll = new JButton("Save All Results");
		jbSaveAll.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JFileChooser fc = new JFileChooser();
				fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				if (fc.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
					String dest = fc.getSelectedFile().toString();
					int keywordRepeatNr = (!jtNrKeywords.getText().equals("")) ? Integer.parseInt(jtNrKeywords.getText()): 0;
					if (jcbSrc.getSelectedIndex() != 1) {
						if (resultList != null && resultList.size() > 0) {
							SaveResults sr = new SaveResults();
							for(Result result: resultList){
								try {
									sr.saveToFile(result.getLink(), jtSearchKey.getText(), jcbSrc.getSelectedIndex(), null, dest,
											null, null, null, null, jcBlogExc.isSelected(), keywordRepeatNr);
								} catch (Exception e1) {
									e1.printStackTrace();
									JOptionPane.showMessageDialog(null, "Due to the network problem the saving process is suspended.",
											"Error", JOptionPane.ERROR_MESSAGE);
								}
							}
						}
					} else {
						try {
							callNyTimesSaveMethod(jtSearchKey.getText(), jcbSrc.getSelectedIndex(),jcBlogExc.isSelected(),
									keywordRepeatNr, dest);
						} catch (Exception e1) {
							e1.printStackTrace();
						}
//						double HitsNum = Double.parseDouble(jlHitsNum.getText());
//						if (HitsNum <= 1010) {
//							SaveResults sr = new SaveResults();
//							try {
//								getContentPane().setCursor(new Cursor(Cursor.WAIT_CURSOR));
//								String k[] = jtSearchKey.getText().split("\\s+");
//								String searchKey = k[0];
//								for (int i = 1; i < k.length; i++) {
//									searchKey += "+" + k[i];
//								}
//								for (int j = startingPage; ; j++) {
//									nyResultList = searchNYT(searchKey, startDatePicker.getModel(), endDatePicker.getModel(), j);
//									int numberOfAllResults = (nyResultList != null) ? nyResultList.size() : 0;
//									if (nyResultList != null && numberOfAllResults > 0) {
//										for(int i = startingArticle; i < numberOfAllResults; i = i+7){
//											sr.saveToFile(nyResultList.get(i), jtSearchKey.getText(), jcbSrc.getSelectedIndex(), nyResultList.get(i+1),
//													dest, nyResultList.get(i+3), nyResultList.get(i+4), nyResultList.get(i+5), nyResultList.get(i+6),
//													jcBlogExc.isSelected(), keywordRepeatNr);
//											startingArticle = startingArticle+7;										
//										}
//										startingArticle = 0;
//										startingPage++;
//									} else {
//										startingArticle = 0;
//										startingPage = 0;
//										break;
//									}
//								}
//							} catch (Exception e1) {
//								e1.printStackTrace();
//								Toolkit.getDefaultToolkit().beep();
////								JOptionPane.showMessageDialog(null, "Due to the network problem the saving process is suspended. " +
////										"To save the rest of results, click on \"Save All Results\" button again", "Error", JOptionPane.ERROR_MESSAGE);
//							}
//							getContentPane().setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
//							Toolkit.getDefaultToolkit().beep();
//							JOptionPane.showMessageDialog(null, "Finished!", "Message", JOptionPane.INFORMATION_MESSAGE);
//						} else {
//							double indexTemp = HitsNum/1010;
//							int indexNum = (int)indexTemp + 1;
//							SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
//							String startS = dateFormat.format(startDatePicker.getModel().getValue());
//							String endS = dateFormat.format(endDatePicker.getModel().getValue());
//							try {
//								Date startD = dateFormat.parse(startS);
//								Date endD = dateFormat.parse(endS);
//								long diff = endD.getTime() - startD.getTime();
//								int interval = (int) (TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS)/indexNum);
//								Calendar cal = Calendar.getInstance();
//								Calendar cal2 = Calendar.getInstance();
//								cal.setTime(startD);
//								cal2.setTime(endD);
//								for (;cal.compareTo(cal2) <= 0;) {
//									cal.add(Calendar.DATE, interval);
//									
//								}
//							} catch(ParseException parseException) {
//								parseException.printStackTrace();
//							}
//			
////							Toolkit.getDefaultToolkit().beep();
////							JOptionPane.showMessageDialog(null, "Saving the results beyond page 100 is not possible. Please refine the search "
////									+ "in the way that the number of results are less than 1000 entries to be able to save all of them",
////									"Error", JOptionPane.ERROR_MESSAGE);
////							startDatePicker.requestFocus();
//						}	
					}					
				}
			}

			//To do recursively the save method
			private void callNyTimesSaveMethod(String text, int srcSelectedIndex, boolean blogSelected,
					int keywordRepeatNr, String dest) {
				String hitsNum = getHitsNum(text, startDatePicker.getModel(), endDatePicker.getModel());
				if (Integer.parseInt(hitsNum) <= 1010) {
					SaveResults sr = new SaveResults();
					try {
						getContentPane().setCursor(new Cursor(Cursor.WAIT_CURSOR));
						String k[] = text.split("\\s+");
						String searchKey = k[0];
						for (int i = 1; i < k.length; i++) {
							searchKey += "+" + k[i];
						}
						for (int j = startingPage; ; j++) {
							nyResultList = searchNYT(searchKey, startDatePicker.getModel(), endDatePicker.getModel(), j);
							int numberOfAllResults = (nyResultList != null) ? nyResultList.size() : 0;
							if (nyResultList != null && numberOfAllResults > 0) {
								for(int i = startingArticle; i < numberOfAllResults; i = i+7){
									sr.saveToFile(nyResultList.get(i), text, srcSelectedIndex, nyResultList.get(i+1),
											dest, nyResultList.get(i+3), nyResultList.get(i+4), nyResultList.get(i+5), nyResultList.get(i+6),
											blogSelected, keywordRepeatNr);
									startingArticle = startingArticle+7;										
								}
								startingArticle = 0;
								startingPage++;
							} else {
								startingArticle = 0;
								startingPage = 0;
								break;
							}
						}
					} catch (Exception e1) {
						e1.printStackTrace();
						Toolkit.getDefaultToolkit().beep();
//						JOptionPane.showMessageDialog(null, "Due to the network problem the saving process is suspended. " +
//								"To save the rest of results, click on \"Save All Results\" button again", "Error", JOptionPane.ERROR_MESSAGE);
					}
					getContentPane().setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
					Toolkit.getDefaultToolkit().beep();
//					JOptionPane.showMessageDialog(null, "Finished!", "Message", JOptionPane.INFORMATION_MESSAGE);
				} else {
					int indexNum = Integer.parseInt(hitsNum)/1000 + 1;
					SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
					String startS = dateFormat.format(startDatePicker.getModel().getValue());
					String endS = dateFormat.format(endDatePicker.getModel().getValue());
					try {
						Date startD = dateFormat.parse(startS);
						Date endD = dateFormat.parse(endS);
						long diff = endD.getTime() - startD.getTime();
						int interval = (int) (TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS)/indexNum);
						Calendar cal = Calendar.getInstance();
						Calendar cal2 = Calendar.getInstance();
						cal.setTime(startD);
						cal2.setTime(endD);
						
						Calendar date = cal;
						//First round of filling in start DatePicker
						startDatePicker.getModel().setDate(date.get(Calendar.YEAR), date.get(Calendar.MONTH), date.get(Calendar.DAY_OF_MONTH));
						//First round of filling in end DatePicker
						cal.add(Calendar.DATE, interval);
						date = cal;
						endDatePicker.getModel().setDate(date.get(Calendar.YEAR), date.get(Calendar.MONTH), date.get(Calendar.DAY_OF_MONTH));
						searchInNYT(text);
						callNyTimesSaveMethod(text, srcSelectedIndex,blogSelected, keywordRepeatNr, dest);
						for (;cal.compareTo(cal2) <= 0;) {
							cal.add(Calendar.DATE, 1);
							date = cal;
							//Fill in start DatePicker
							startDatePicker.getModel().setDate(date.get(Calendar.YEAR), date.get(Calendar.MONTH), date.get(Calendar.DAY_OF_MONTH));
							//Fill in end DatePicker
							cal.add(Calendar.DATE, interval);
							if (cal.compareTo(cal2) <= 0) {
								date = cal;
								endDatePicker.getModel().setDate(date.get(Calendar.YEAR), date.get(Calendar.MONTH), date.get(Calendar.DAY_OF_MONTH));
							} else {
								date = cal2;
								endDatePicker.getModel().setDate(date.get(Calendar.YEAR), date.get(Calendar.MONTH), date.get(Calendar.DAY_OF_MONTH));
							}
							searchInNYT(text);
							callNyTimesSaveMethod(text, srcSelectedIndex,blogSelected, keywordRepeatNr, dest);
						}
					} catch(Exception e) {
						e.printStackTrace();
					}

//					Toolkit.getDefaultToolkit().beep();
//					JOptionPane.showMessageDialog(null, "Saving the results beyond page 100 is not possible. Please refine the search "
//							+ "in the way that the number of results are less than 1000 entries to be able to save all of them",
//							"Error", JOptionPane.ERROR_MESSAGE);
//					startDatePicker.requestFocus();
				}		
			}
		});
		jbSaveAll.setFont(new Font("Tahoma", Font.BOLD, 12));
		getContentPane().add(jbSaveAll, BorderLayout.WEST);
		jbSaveAll.setBounds(1220, 675, 135, 25);
		jbSaveAll.setEnabled(false);
	}
	
	private void searchInNYT(String text) {
		jtaResult.setText("");
		page = 0;
		try {
			getContentPane().setCursor(new Cursor(Cursor.WAIT_CURSOR));
			String k[] = text.split("\\s+");
			String searchKey = k[0];
			for (int i = 1; i < k.length; i++) {
				searchKey += "+" + k[i];
			}
			nyResultList = searchNYT(searchKey, startDatePicker.getModel(), endDatePicker.getModel(), page);
			jlHitsNum.setText(getHitsNum(searchKey, startDatePicker.getModel(), endDatePicker.getModel()));
			jlPageNum.setText(String.valueOf(page+1));
			for(int i = 0; i < nyResultList.size(); i = i+7){
				jtaResult.setText(jtaResult.getText() + nyResultList.get(i) + "\n");
				jtaResult.setText(jtaResult.getText() + nyResultList.get(i+1) + "\n");
				jtaResult.setText(jtaResult.getText() + nyResultList.get(i+2) + "\n");
				jtaResult.setText(jtaResult.getText() + "\n");
			}
			if (nyResultList != null && nyResultList.size() > 0) {
				jbPre.setEnabled(false);
				jbNext.setEnabled(true);
				jbSave.setEnabled(true);
				jbSaveAll.setEnabled(true);
			}
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		getContentPane().setCursor(new Cursor(Cursor.DEFAULT_CURSOR));				
	}

	//To search in Washington Post or Yahoo MB
	private List<Result> search(String searchKey, int srcNum, Object startDate, Object endDate, long pageNum) {
		SearchClient gsc = new SearchClient();
		if (startDate != null) {
			Date startD = (Date) startDate;
			Calendar sCal = Calendar.getInstance();
			sCal.setTime(startD);
			long startRange = (long) JulianDate.dateToJulian(sCal);
			Date endD = (Date) endDate;
			sCal.setTime(endD);
			long endRange = (long) JulianDate.dateToJulian(sCal);
			String timedSearchKey = searchKey + " daterange:" + startRange + "-" + endRange; 
			List<Result> list = gsc.getSearchResult(timedSearchKey, srcNum, pageNum);
			return list;
		} else {
			List<Result> list = gsc.getSearchResult(searchKey, srcNum, pageNum);
			return list;
		}			
	}
	
	//To get the number of results in New York Times
	@SuppressWarnings("rawtypes")
	private String getHitsNum(String searchKey, DateModel startDate, DateModel endDate) {
		String startD = null;
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
		String endD = dateFormat.format(endDate.getValue());
		long hitsNum = 0;
		if (startDate.getValue() == null) {
			startD = "19950101";
		} else {
			startD = dateFormat.format(startDate.getValue());
		}
		try {
			hitsNum = RestAPI.getHitsNum(searchKey, startD, endD);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return String.valueOf(hitsNum);
	}
	
	//To search in New York Times
	@SuppressWarnings("rawtypes")
	private List<String> searchNYT(String searchKey, DateModel startDate, DateModel endDate, long page) {
		String startD = null;
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
		String endD = dateFormat.format(endDate.getValue());
		List<String> list = null;
		if (startDate.getValue() == null) {
			startD = "19950101";
		} else {
			startD = dateFormat.format(startDate.getValue());
		}
		try {
			list = RestAPI.search(searchKey, startD, endD, page);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return list;
	}
}
