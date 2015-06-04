package zach.jconsole;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GraphicsEnvironment;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.UIManager;

public class JConsole {
	
	public static boolean PARSE_IGNORE_CAPS = false;
	public static final double version = 1.d;
	
	public interface Action {
		public void perform(JConsole console, String[] args);
	}
	public class Command {
		private String command;
		private Action action;
		public Command(String command, Action action) {
			this.command = command;
			this.action = action;
		}
		public final void perform(JConsole console, String[] args) { action.perform(console, args); }
		public final String getCommand() { return command; }
		public String getHelpString() { return "Default Help String"; }
		public String toString() { return getCommand(); }
	}
	
	private Map<String, Command> commandsMap = new HashMap<String, Command>();
	
	private JFrame window;
	private JTextArea textField;
	private Font font = new Font("CONSOLAS", 0, 12);
	private int alpha;
	private JScrollPane scroll;
	private String lastCommand = "";
	
	private JMenuBar menu;
	private int[] prevPos = new int[2];
	private JLabel xButton;
	private JLabel minButton;
	
	private boolean resize;
	private JPanel east;
	private JPanel south;
	private JPanel west;
	private JPanel sw;
	private JPanel se;
	
	public JConsole(String title, String initText, boolean useDefaultCommands, boolean exitOnClose) {
		alpha = 0x99;
		
		commandsMap.put("?", new Command("?", new Action() {
			public void perform(JConsole console, String[] args) {
				if (args.length == 1) {
					console.print("\n" + "Java Console version " + version
						+ "\n" + "Type \"? <command>\" to learn its usage"
						+ "\n" + "Commands currently active:");
					String commandList = "";
					for (Entry<String, Command> e : commandsMap.entrySet())
						commandList += "\n" + e.getKey();
					console.print(commandList);
				} else {
					Command c = commandsMap.get(args[1]);
					if (c != null)
						console.print("\n" + c.getHelpString());
					else
						console.print("\n" + "\"" + args[1] + "\" is not recognized as a command");
				}
			};
		}) {
			public String getHelpString() {
				return "Provides help with command usage"
					 + "\narg1 = Command";
			}
		});
		if (useDefaultCommands)
			initCommands();
		
		window = new JFrame(title) {
			private static final long serialVersionUID = 1L;
			public void paint(Graphics g) {
				g.clearRect(2, menu.getHeight() + 2, getWidth() - 4, getHeight()-menu.getHeight() - 4);
				g.setColor(getBackground());
				g.fillRect(2, menu.getHeight() + 2, getWidth() - 4, getHeight()-menu.getHeight() - 4);
				super.paint(g);
			}
		};
		textField = new JTextArea(initText) {
			private static final long serialVersionUID = 1L;
			public void paintComponent(Graphics g) {
				g.clearRect(0, 0, textField.getWidth(), textField.getHeight());
				g.setColor(getBackground());
				g.fillRect(0, 0, textField.getWidth(), textField.getHeight());
				super.paintComponent(g);
				window.repaint();
			}
		};
		textField.setOpaque(false);
		menu = new JMenuBar();
		xButton = new JLabel(" X ");
		minButton = new JLabel(" _ ");
		newLine(false);
		
		//Window
		window.setDefaultCloseOperation(exitOnClose ? JFrame.EXIT_ON_CLOSE
												 : JFrame.DISPOSE_ON_CLOSE);
		window.setResizable(false);
		window.setUndecorated(true);
		window.setJMenuBar(menu);
		window.setBackground(new Color(0, 0, 0, alpha));
		
		BufferedImage bi = new BufferedImage(256, 256, BufferedImage.TYPE_INT_ARGB); //Making Image Icon
		Graphics g = bi.getGraphics();
		g.setColor(Color.decode("#0FFFF0")); //Just a nice green
		g.fillRoundRect(10, 10, 236, 236, 100, 100);
		window.setIconImage(bi);
		
		JLabel image = new JLabel(new ImageIcon(bi)) {
			private static final long serialVersionUID = 1L;
			public void paintComponent(Graphics g) {
				g.drawImage(bi, 0, 0, 23, 23, null);
			}
		};
		image.setPreferredSize(new Dimension(25, 23));
		menu.add(image);
		menu.add(new JLabel(title));
		menu.setBackground(Color.LIGHT_GRAY);
		menu.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 4));
		menu.addMouseListener(new MouseListener() {
			public void mouseClicked(MouseEvent e) {}
			public void mouseEntered(MouseEvent e) {}
			public void mouseExited(MouseEvent e) {}
			public void mousePressed(MouseEvent e) {
				prevPos[0] = e.getX();
				prevPos[1] = e.getY();
			}
			public void mouseReleased(MouseEvent e) {
				if (window.getY() < 0)
					window.setLocation(window.getX(), 0);
				else if (window.getY() >= GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds().height - menu.getHeight())
					window.setLocation(window.getX(),
							GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds().height - menu.getHeight());
			}
		});
		menu.addMouseMotionListener(new MouseMotionListener() {
			public void mouseDragged(MouseEvent e) {
				window.setLocation(window.getX() + e.getX() - prevPos[0], window.getY() + e.getY() - prevPos[1]);
			}
			public void mouseMoved(MouseEvent e) {}
		});
		menu.add(Box.createHorizontalGlue());
		menu.add(minButton);
		minButton.setBorder(BorderFactory.createLineBorder(new Color(0x334433)));
		minButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
		minButton.addMouseListener(new MouseListener() {
			public void mouseClicked(MouseEvent e) {
				window.setState(JFrame.ICONIFIED);
			}
			public void mouseEntered(MouseEvent e) {}
			public void mouseExited(MouseEvent e) {}
			public void mousePressed(MouseEvent e) {}
			public void mouseReleased(MouseEvent e) {}
		});
		menu.add(xButton);
		xButton.setBorder(BorderFactory.createLineBorder(new Color(0x334433)));
		xButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
		xButton.addMouseListener(new MouseListener() {
			public void mouseClicked(MouseEvent e) {
				window.dispose();
			}
			public void mouseEntered(MouseEvent e) {}
			public void mouseExited(MouseEvent e) {}
			public void mousePressed(MouseEvent e) {}
			public void mouseReleased(MouseEvent e) {}
		});
		menu.add(new JLabel("  "));
		
		try {
			UIManager.setLookAndFeel(
					UIManager.getSystemLookAndFeelClassName()); //Native look and feel
		} catch(Exception e) {
			try {
				UIManager.setLookAndFeel(
						UIManager.getCrossPlatformLookAndFeelClassName());
			} catch (Exception e1) {
				e.printStackTrace();
			}
		}
		
		//Text Area
		textField.setSize(400, 253);
		textField.setFont(font);
		textField.setBackground(new Color(0, 0, 0, alpha));
		textField.setForeground(new Color(0xFF, 0xFF, 0xFF, 0xFF));
		textField.setCaretColor(new Color(0xFF, 0xFF, 0xFF, 0xFF));
		textField.setLineWrap(false);
		//Text Area Event Listeners
		textField.addKeyListener(new KeyListener() {
			public void keyPressed(KeyEvent e) {
				switch (e.getKeyCode()) {
				case KeyEvent.VK_ENTER: //Override keys
					e.consume();
					newLine(true);
					break;
				case KeyEvent.VK_BACK_SPACE:
					if (!canBackSpace())
						e.consume();
					break;
				case KeyEvent.VK_LEFT:
					if (!canBackSpace())
						e.consume();
					break;
				case KeyEvent.VK_UP:
					e.consume();
					break;
				case KeyEvent.VK_DOWN:
					e.consume();
					break;
				case KeyEvent.VK_TAB:
					e.consume();
					copyLastCommand();
					break;
				}
			}
			public void keyReleased(KeyEvent e) {}
			public void keyTyped(KeyEvent e) {
				if (textField.getCaretPosition() < getLastLine())
					e.consume();
				switch (e.getKeyChar()) {
				case '>' :
					e.consume();
					break;
				}
			}
		});
		textField.addMouseListener(new MouseListener() {
			public void mouseClicked(MouseEvent e) {
				e.consume();
				if (textField.getCaretPosition() < getLastLine())
					textField.setCaretPosition(textField.getText().length());
			}
			public void mouseEntered(MouseEvent e) {}
			public void mouseExited(MouseEvent e) {}
			public void mousePressed(MouseEvent e) {}
			public void mouseReleased(MouseEvent e) {}
		});
		textField.addMouseMotionListener(new MouseMotionListener() {
			public void mouseDragged(MouseEvent e) {
				int lastLine = getLastLine();
				if (textField.getCaretPosition() < lastLine)
					textField.setCaretPosition(lastLine);
			}
			public void mouseMoved(MouseEvent e) {}
		});
		
		
		//Scroll Pane
		scroll = new JScrollPane(textField);
		scroll.setBorder(null);
		
		//Resize grabbing stuff
		west = new JPanel();
		west.setBackground(Color.LIGHT_GRAY);
		west.setPreferredSize(new Dimension(5, 100));
		west.setCursor(new Cursor(Cursor.W_RESIZE_CURSOR));
		west.addMouseListener(new MouseListener() {
			public void mouseClicked(MouseEvent e) {}
			public void mouseReleased(MouseEvent e) {}
			public void mouseEntered(MouseEvent e) {}
			public void mouseExited(MouseEvent e) {}
			public void mousePressed(MouseEvent e) {
				prevPos[0] = e.getX();
			}
		});
		west.addMouseMotionListener(new MouseMotionListener() {
			public void mouseMoved(MouseEvent e) {}
			public void mouseDragged(MouseEvent e) {
				if (resize) {
					int newSize = -e.getX() + window.getWidth() + prevPos[0];
					if (newSize < 230) return;
					window.setLocation(e.getXOnScreen() - prevPos[0], window.getY());
					window.setSize(newSize, window.getHeight());
				}
			}
		});
		
		east = new JPanel();
		east.setBackground(Color.LIGHT_GRAY);
		east.setPreferredSize(new Dimension(5, 100));
		east.setCursor(new Cursor(Cursor.E_RESIZE_CURSOR));
		east.addMouseListener(new MouseListener() {
			public void mouseClicked(MouseEvent e) {}
			public void mouseReleased(MouseEvent e) {}
			public void mouseEntered(MouseEvent e) {}
			public void mouseExited(MouseEvent e) {}
			public void mousePressed(MouseEvent e) {
				prevPos[0] = e.getX();
			}
		});
		east.addMouseMotionListener(new MouseMotionListener() {
			public void mouseMoved(MouseEvent e) {}
			public void mouseDragged(MouseEvent e) {
				if (resize) {
					int newSize = e.getX() + window.getWidth() - prevPos[0];
					if (newSize < 230) newSize = 230;
					window.setSize(newSize, window.getHeight());
				}
			}
		});
		
		south = new JPanel();
		south.setBackground(Color.LIGHT_GRAY);
		south.setPreferredSize(new Dimension(100, 5));
		south.setCursor(new Cursor(Cursor.S_RESIZE_CURSOR));
		south.addMouseListener(new MouseListener() {
			public void mouseClicked(MouseEvent e) {}
			public void mouseReleased(MouseEvent e) {}
			public void mouseEntered(MouseEvent e) {}
			public void mouseExited(MouseEvent e) {}
			public void mousePressed(MouseEvent e) {
				prevPos[1] = e.getY();
			}
		});
		south.addMouseMotionListener(new MouseMotionListener() {
			public void mouseMoved(MouseEvent e) {}
			public void mouseDragged(MouseEvent e) {
				if (resize) {
					int newSize = e.getY() + window.getHeight() - prevPos[1];
					if (newSize < 50) newSize = 50;
					window.setSize(window.getWidth(), newSize);
				}
			}
		});
		
		sw = new JPanel();
		sw.setBackground(Color.LIGHT_GRAY);
		sw.setPreferredSize(new Dimension(5, 5));
		sw.setCursor(new Cursor(Cursor.SW_RESIZE_CURSOR));
		sw.addMouseListener(new MouseListener() {
			public void mouseClicked(MouseEvent e) {}
			public void mouseReleased(MouseEvent e) {}
			public void mouseEntered(MouseEvent e) {}
			public void mouseExited(MouseEvent e) {}
			public void mousePressed(MouseEvent e) {
				south.getMouseListeners()[0].mousePressed(e);
				west.getMouseListeners()[0].mousePressed(e);
			}
		});
		sw.addMouseMotionListener(new MouseMotionListener() {
			public void mouseMoved(MouseEvent e) {}
			public void mouseDragged(MouseEvent e) {
				south.getMouseMotionListeners()[0].mouseDragged(e);
				west.getMouseMotionListeners()[0].mouseDragged(e);
			}
		});
		
		se = new JPanel();
		se.setBackground(Color.LIGHT_GRAY);
		se.setPreferredSize(new Dimension(5, 5));
		se.setCursor(new Cursor(Cursor.SE_RESIZE_CURSOR));
		se.addMouseListener(new MouseListener() {
			public void mouseClicked(MouseEvent e) {}
			public void mouseReleased(MouseEvent e) {}
			public void mouseEntered(MouseEvent e) {}
			public void mouseExited(MouseEvent e) {}
			public void mousePressed(MouseEvent e) {
				south.getMouseListeners()[0].mousePressed(e);
				east.getMouseListeners()[0].mousePressed(e);
			}
		});
		se.addMouseMotionListener(new MouseMotionListener() {
			public void mouseMoved(MouseEvent e) {}
			public void mouseDragged(MouseEvent e) {
				south.getMouseMotionListeners()[0].mouseDragged(e);
				east.getMouseMotionListeners()[0].mouseDragged(e);
			}
		});
		
		south.setLayout(new BorderLayout());
		south.add(sw, BorderLayout.WEST);
		south.add(se, BorderLayout.EAST);
		
		//Finalize window
		window.add(scroll, BorderLayout.CENTER);
		window.add(west, BorderLayout.WEST);
		window.add(east, BorderLayout.EAST);
		window.add(south, BorderLayout.SOUTH);
		window.setSize(textField.getSize());
		window.setLocationRelativeTo(null);
		window.setVisible(true);
	}
	
	public void setResizable(boolean resizable) {
		resize = resizable;
		south.setCursor(new Cursor(resizable ? Cursor.S_RESIZE_CURSOR : Cursor.DEFAULT_CURSOR));
		west.setCursor(new Cursor(resizable ? Cursor.W_RESIZE_CURSOR : Cursor.DEFAULT_CURSOR));
		east.setCursor(new Cursor(resizable ? Cursor.E_RESIZE_CURSOR : Cursor.DEFAULT_CURSOR));
		sw.setCursor(new Cursor(resizable ? Cursor.SW_RESIZE_CURSOR : Cursor.DEFAULT_CURSOR));
		se.setCursor(new Cursor(resizable ? Cursor.SE_RESIZE_CURSOR : Cursor.DEFAULT_CURSOR));
	}
	
	private int getLastLine() {
		int lastLine = textField.getText().length();
		for (int i = lastLine-1; i >= 0; i--)
			if (textField.getText().charAt(i) == '\n' || textField.getText().charAt(i) == '>') {
				lastLine = i+1;
				break;
			}
		return lastLine;
	}
	
	protected void initCommands() { //Default Commands
		commandsMap.put("echo", new Command("echo", new Action() {
			public void perform(JConsole console, String[] args) {
				String print = "\n";
				for (int i = 1; i < args.length; i++)
					print += args[i] + " ";
				console.print(print);
			};
		}) {
			public String getHelpString() {
				return "Echos string of text"
					 + "\nargs = String to echo";
			}
		});
		commandsMap.put("exit", new Command("exit", new Action() {
			public void perform(JConsole console, String[] args) {
				console.window.dispose();
			};
		}) {
			public String getHelpString() {
				return "Closes the console";
			}
		});
		commandsMap.put("cls", new Command("cls", new Action() {
			public void perform(JConsole console, String[] args) {
				console.textField.setText("");
			};
		}) {
			public String getHelpString() {
				return "Clears the console";
			}
		});
		commandsMap.put("color", new Command("color", new Action() {
			public void perform(JConsole console, String[] args) {
				try {
				if (args[1].equalsIgnoreCase("bg")) {
					if (!args[2].contains("#"))
						args[2] = "#" + args[2];
					Color col = Color.decode(args[2]);
					col = new Color(col.getRed(), col.getGreen(), col.getBlue(), alpha);
					console.textField.setBackground(col);
				}
				else if (args[1].equalsIgnoreCase("fg")) {
					if (!args[2].contains("#"))
						args[2] = "#" + args[2];
					console.textField.setForeground(Color.decode(args[2]));
					console.textField.setCaretColor(Color.decode(args[2]));
				}
				else if (args[1].equalsIgnoreCase("border")) {
					if (!args[2].contains("#"))
						args[2] = "#" + args[2];
					Color col = Color.decode(args[2]);
					console.menu.setBackground(col);
					console.menu.setBorder(BorderFactory.createLineBorder(col, 4));
					console.south.setBackground(col);
					console.west.setBackground(col);
					console.east.setBackground(col);
					console.sw.setBackground(col);
					console.se.setBackground(col);
				}
				else if (args[1].equalsIgnoreCase("default")) {
					console.menu.setBackground(Color.LIGHT_GRAY);
					console.menu.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 4));
					console.south.setBackground(Color.LIGHT_GRAY);
					console.west.setBackground(Color.LIGHT_GRAY);
					console.east.setBackground(Color.LIGHT_GRAY);
					console.sw.setBackground(Color.LIGHT_GRAY);
					console.se.setBackground(Color.LIGHT_GRAY);
					console.textField.setForeground(Color.WHITE);
					console.textField.setCaretColor(Color.WHITE);
					console.textField.setBackground(new Color(0, 0, 0, alpha));
				}
				} catch (ArrayIndexOutOfBoundsException e) {}
				  catch (NumberFormatException e) {}
			};
		}) {
			public String getHelpString() {
				return "Change the console colors"
					 + "\narg1 = bg | fg | border | default"
					 + "\narg2 = color (hexadecimal)";
			}
		});
		commandsMap.put("alpha", new Command("alpha", new Action() {
			public void perform(JConsole console, String[] args) {
				try {
					int alpha = Integer.parseInt(args[1]);
					if (alpha >= 255)
						alpha = 254;
					console.alpha = alpha;
					Color winCol = console.window.getBackground();
					console.window.setBackground(new Color(winCol.getRed(), winCol.getGreen(), winCol.getBlue(), alpha));
					Color consCol = console.textField.getBackground();
					console.textField.setBackground(new Color(consCol.getRed(), consCol.getGreen(), consCol.getBlue(), alpha));
				} catch (ArrayIndexOutOfBoundsException | NumberFormatException e) {}
			}
		}) {
			public String getHelpString() {
				return "Sets the alpha of the console"
					 + "\narg1 = alpha value (0 - 255)";
			}
		});
	}
	
	public void newLine(boolean parse) { //Standard for new line + command parsing
		if (parse) {
			String[] lines = textField.getText().split("\n");
			String line = lines[lines.length-1];
			parseCommand(line.substring(1));
		}
		if (textField.getText().equalsIgnoreCase(""))
			textField.setText(textField.getText() + ">");
		else
			textField.setText(textField.getText() + "\n>");
		
		textField.setCaretPosition(textField.getText().length());
	}
	public boolean canBackSpace() { //Standard for back spacing
		if (textField.getText().charAt(textField.getCaretPosition()-1) == '>')
			return false;
		return true;
	}
	public void copyLastCommand() {
		if (!lastCommand.equalsIgnoreCase("")) {
			print(lastCommand);
			textField.setCaretPosition(textField.getText().length()-lastCommand.length());
			textField.moveCaretPosition(textField.getText().length());
		}
	}
	public void parseCommand(String line) { //Command parsing
		line = line.trim();
		String[] args = line.split(" ");
		if (!line.equalsIgnoreCase("")) {
			List<String> list = new LinkedList<String>(Arrays.asList(args));
			for (int i = list.size()-1; i >= 0; i--) {
				list.set(i, list.get(i).trim());
				if (list.get(i).equalsIgnoreCase(""))
					list.remove(i);
			}
			lastCommand = "";
			for (String s : list)
				lastCommand += s + " ";
			lastCommand = lastCommand.trim();
			Command c = commandsMap.get(list.get(0));
			if (c != null)
				c.perform(this, list.toArray(new String[0]));
			else
				print("\n\"" + list.get(0) + "\" is not recognized as a command");
		}
	}
	
	public void addCommand(Command c) {
		commandsMap.put(c.getCommand(), c);
	}
	
	public void print(String text) {
		textField.setText(textField.getText() + text);
	}
	
	public static void main(String args[]) { //Just a test main, do not use this in application
		JConsole.PARSE_IGNORE_CAPS = true;
		new JConsole("Java Console [DEFAULT]", "Default Java Console by Zachary Wells\nType '?' for help", true, true).setResizable(true);
	}
}
