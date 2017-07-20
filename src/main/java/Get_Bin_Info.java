//Read Koala Bin Header information stored in FileInfo > debugInfo String
import ij.*;
import ij.io.*;
import ij.plugin.*;

public class Get_Bin_Info implements PlugIn {
    public void run(String arg) {
    	ImagePlus img = IJ.getImage(); 
    	FileInfo fi = img.getOriginalFileInfo();
    	IJ.showMessage(fi.debugInfo);
    	return;
    }
}
