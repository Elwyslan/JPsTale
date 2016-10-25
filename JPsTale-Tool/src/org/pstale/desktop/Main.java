package org.pstale.desktop;

import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.TextArea;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Callable;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSlider;
import javax.swing.JTabbedPane;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.pstale.app.LoaderApp;
import org.pstale.state.AxisAppState;
import org.pstale.state.GuiLoaderAppState;
import org.pstale.state.LoaderAppState;
import org.pstale.utils.FileChooser;
import org.pstale.utils.FolderChooser;
import org.pstale.utils.SkeletonToTree;

import com.jme3.animation.AnimControl;
import com.jme3.animation.Skeleton;
import com.jme3.system.AppSettings;
import com.jme3.system.JmeCanvasContext;

/**
 * ������
 * 
 * @author yanmaoyuan
 * 
 */
public class Main extends JFrame {

	private static final long serialVersionUID = 1L;

	private JmeCanvasContext context;
	private Canvas canvas;
	private LoaderApp app;

	private TextArea console;
	private FileChooser fileChooser = new FileChooser();
	private FolderChooser folderChooser = new FolderChooser();
	
	private JComboBox<String> combo;
	private JTree tree;

	public Main() {
		this.setTitle("Model Viewer");
		this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		this.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosed(WindowEvent e) {
				app.stop();
			}
		});

		setupUI();
		createMenu();

		pack();
		setLocationRelativeTo(null);
		setVisible(true);
	}

	/**
	 * ���沼��
	 */
	private void setupUI() {

		Container main = getContentPane();
		
		Container canvasPanel = new JPanel();
		canvasPanel.setLayout(new BorderLayout());
		main.add(canvasPanel, BorderLayout.CENTER);
		
		// jME3��Ⱦ��
		createCanvas();
		canvasPanel.add(canvas, BorderLayout.CENTER);

		// �ײ�����̨
		console = new TextArea();
		console.setBackground(Color.WHITE);
		console.setEditable(false);
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setViewportView(console);
		canvasPanel.add(scrollPane, BorderLayout.SOUTH);

		// ���Լ������ص�OutputStream����һ��PrintStream
		PrintStream printStream = new PrintStream(new MyOutputStream());
		// ָ����׼������Լ�������PrintStream
		System.setOut(printStream);
		System.setErr(printStream);

		// �Ҳ�����
		JTabbedPane tabs = new JTabbedPane();
		tabs.setPreferredSize(new Dimension(240, 0));
		tabs.add("�������", createCamera());
		
		main.add(tabs, BorderLayout.EAST);

	}

	/**
	 * �����������һ��TextArea�С�
	 * @author yanmaoyuan
	 *
	 */
	public class MyOutputStream extends OutputStream {
		public void write(int arg0) throws IOException {
			// д��ָ�����ֽڣ�����
		}

		public void write(byte data[]) throws IOException {
			// ׷��һ���ַ���
			console.append(new String(data));
		}

		public void write(byte data[], int off, int len) throws IOException {
			// ׷��һ���ַ�����ָ���Ĳ��֣��������Ҫ
			console.append(new String(data, off, len));
			// �ƶ�TextArea�Ĺ�굽���ʵ���Զ�����
			console.setCaretPosition(console.getText().length());
		}
	}

	/**
	 * �����˵�
	 */
	private void createMenu() {
		JMenuBar menuBar = new JMenuBar();
		setJMenuBar(menuBar);

		JMenu menuFile = new JMenu("�ļ�(F)");
		menuFile.setMnemonic(KeyEvent.VK_F);
		menuBar.add(menuFile);

		final JMenuItem itemLoad = new JMenuItem("����ģ���ļ�(L)");
		itemLoad.setMnemonic(KeyEvent.VK_L);
		itemLoad.setAccelerator(KeyStroke.getKeyStroke("L"));
		menuFile.add(itemLoad);
		itemLoad.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				loadModel();
			}
		});

		final JMenuItem itemSaveAs = new JMenuItem("����ƽ��ͼ(S)");
		itemSaveAs.setMnemonic(KeyEvent.VK_S);
		itemSaveAs.setAccelerator(KeyStroke.getKeyStroke("ctrl S"));
		menuFile.add(itemSaveAs);
		itemSaveAs.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// TODO
				loadGui();
			}
		});

		menuFile.add(new JSeparator());

		JMenuItem itemExit = new JMenuItem("�˳�(X)");
		itemSaveAs.setMnemonic(KeyEvent.VK_X);
		itemExit.setAccelerator(KeyStroke.getKeyStroke("ctrl W"));
		menuFile.add(itemExit);
		itemExit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				dispose();
				app.stop();
			}
		});

		JMenu menuView = new JMenu("��ͼ(V)");
		menuView.setMnemonic(KeyEvent.VK_V);
		menuBar.add(menuView);

		final JMenuItem itemAxis = new JMenuItem("�ر�������");
		itemAxis.setMnemonic(KeyEvent.VK_F4);
		menuView.add(itemAxis);
		itemAxis.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				app.enqueue(new Callable<Void>() {
					public Void call() {
						AxisAppState axis = app.getStateManager().getState(AxisAppState.class);
						if (axis.toggleAxis()) {
							itemAxis.setText("�ر�������");
						} else {
							itemAxis.setText("��������");
						}
						return null;
					}
				});
			}
		});

		JMenuItem itemWireframe = new JMenuItem("Wireframe");
		itemWireframe.setMnemonic(KeyEvent.VK_F3);
		menuView.add(itemWireframe);
		itemWireframe.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				wireframe();
			}
		});

		JMenu menuOption = new JMenu("ѡ��(O)");
		menuOption.setMnemonic(KeyEvent.VK_O);
		menuBar.add(menuOption);

		JMenuItem itemSetRootPath = new JMenuItem("������Դ��Ŀ¼");
		menuOption.add(itemSetRootPath);
		itemSetRootPath.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				setRootPath();
			}
		});

		JMenu menuHelp = new JMenu("����(H)");
		menuHelp.setMnemonic(KeyEvent.VK_H);
		menuBar.add(menuHelp);

		JMenuItem itemAbout = new JMenuItem("����(A)");
		itemAbout.setMnemonic(KeyEvent.VK_A);
		menuHelp.add(itemAbout);
		itemAbout.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				JOptionPane.showMessageDialog(null, 
						new Object[]{"W/S-�����ǰ/���ƶ�", "A/D-�������/���ƶ�", "Q/Z-�������/���ƶ�", "C-�鿴�����λ�ò���", "M-�鿴�ڴ����", "F5-��/�ر�״̬���"},
						"����˵��",
						JOptionPane.INFORMATION_MESSAGE);
			}
		});
	}

	/**
	 * ����jME3��Canvas
	 */
	private void createCanvas() {
		AppSettings settings = new AppSettings(true);
		settings.setWidth(800);
		settings.setHeight(600);

		app = new LoaderApp(this);
		app.setPauseOnLostFocus(false);
		app.setSettings(settings);
		app.createCanvas();
		app.startCanvas();

		context = (JmeCanvasContext) app.getContext();
		canvas = context.getCanvas();
		canvas.setSize(settings.getWidth(), settings.getHeight());
	}

	/**
	 * ������������
	 * @return
	 */
	private Container createCamera() {
		JPanel panel = new JPanel(new BorderLayout());
		
		// ���������
		JPanel p1 = new JPanel(new FlowLayout(FlowLayout.LEFT));
		panel.add(p1, BorderLayout.SOUTH);

		p1.add(new JLabel("��ͷ�ƶ��ٶ�:"));
		final JLabel spdLb = new JLabel("50");
		p1.add(spdLb);
		
		final JSlider slider = new JSlider(JSlider.HORIZONTAL, 5, 100, 50);
		p1.add(slider);
		slider.setMajorTickSpacing(10);
		slider.setPaintLabels(true);
		slider.setPaintTicks(true);
		slider.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				int speed = slider.getValue();
				spdLb.setText(speed+"");
				setMoveSpeed(speed);
			}
		});
		
		// ����
		JScrollPane scrollPane = new JScrollPane();
		tree = new JTree();
		scrollPane.setViewportView(tree);
		panel.add(scrollPane, BorderLayout.CENTER);
		
		// ѡ�񶯻����
		JPanel p2 = new JPanel(new FlowLayout(FlowLayout.LEFT));
		panel.add(p2, BorderLayout.NORTH);
		
		p2.add(new JLabel("����:"));
		combo = new JComboBox<String>();
		p2.add(combo);
		JButton btn = new JButton("����");
		btn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Object obj = combo.getSelectedItem();
				if (obj != null) {
					String name = (String)obj;
					play(name);
				}
			}
		});
		p2.add(btn);
		
		return panel;
	}

	/**
	 * ���Ŷ���
	 * @param name
	 */
	protected void play(final String name) {
		app.enqueue(new Callable<Void>() {
			public Void call() {
				LoaderAppState state = app.getStateManager().getState(LoaderAppState.class);
				state.play(name);
				return null;
			}
		});
	}
	
	/**
	 * ��������
	 */
	void wireframe() {
		app.enqueue(new Callable<Void>() {
			public Void call() {
				LoaderAppState state = app.getStateManager().getState(LoaderAppState.class);
				state.wireframe();
				return null;
			}
		});
	}
	
	/**
	 * ��������ϵ
	 */
	void axis() {
		
	}

	/**
	 * ����ģ��
	 */
	private void loadModel() {
		File file = fileChooser.getFile();
		if (file != null) {
			final String path = file.getAbsolutePath();
			app.enqueue(new Callable<Void>() {
				public Void call() {
					combo.removeAllItems();
					
					LoaderAppState state = app.getStateManager().getState(LoaderAppState.class);
					state.loadModel(path);
					
					AnimControl ac = state.getAnimControl();
					if (ac != null) {
						Skeleton ske = ac.getSkeleton();
						SkeletonToTree stt = new SkeletonToTree();
						tree.setModel(stt.make(ske));
					}
					
					return null;
				}
			});

		}
	}
	
	/**
	 * ����ģ��
	 */
	private void loadGui() {
		File file = fileChooser.getFile();
		if (file != null) {
			final String path = file.getAbsolutePath();
			app.enqueue(new Callable<Void>() {
				public Void call() {
					combo.removeAllItems();
					
					GuiLoaderAppState state = app.getStateManager().getState(GuiLoaderAppState.class);
					state.loadModel(path);
					
					return null;
				}
			});

		}
	}
	
	/**
	 * ������Ϸ��Ŀ¼
	 */
	private void setRootPath() {
		File file = folderChooser.getFile();
		if (file != null) {
			final String path = file.getAbsolutePath();
			app.enqueue(new Callable<Void>() {
				public Void call() {
					LoaderAppState state = app.getStateManager().getState(LoaderAppState.class);
					state.setRootpath(path);
					
					return null;
				}
			});

		}
	}
	
	/**
	 * ������������ƶ��ٶ�
	 * @param speed
	 */
	private void setMoveSpeed(final int speed) {
		app.enqueue(new Callable<Void>() {
			public Void call() {
				app.getFlyByCamera().setMoveSpeed(speed);
				return null;
			}
		});
		
	}

	public void setAnimList(Collection<String> collection) {
		List<String> names = new ArrayList<String>();
		names.addAll(collection);
		names.sort(new Comparator<String>() {
			@Override
			public int compare(String a, String b) {
				int i = a.indexOf(" ");
				int j = b.indexOf(" ");
				int n = Integer.parseInt(a.substring(0, i));
				int m = Integer.parseInt(b.substring(0, j));
				return n-m;
			}});
		for(String name : names) {
			combo.addItem(name);
		}
	}
	
	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				JPopupMenu.setDefaultLightWeightPopupEnabled(false);
				new Main();
			}
		});
	}

}
