package zach.jconsole;

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