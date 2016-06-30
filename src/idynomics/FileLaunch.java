package idynomics;

import java.io.File;

import javax.swing.SwingUtilities;

import guiTools.GuiActions;

public class FileLaunch {

	public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            GuiLaunch app = new GuiLaunch();
            if (args.length > 0) {
                File f = new File(args[0]);
                if (f.isFile()) {
                    GuiActions.openFile(f);
                }
            }
        });
    }
}
