import ij.*;
import ij.gui.*;
import ij.plugin.*;
import ij.process.*;
import ij.measure.*;
import java.awt.*;
import java.io.*;
import java.nio.*;
import ij.io.*;
import java.util.*;

public class Koala_Sequence_Reader extends ImagePlus implements PlugIn {
	
	public void run(String arg) {  
        String path = getPath(arg);  
        if (null == path) return;  
        if (!parse(path)) return;  
        if (null == arg || 0 == arg.trim().length()) this.show(); // was opened by direct call to the plugin  
                          // not via HandleExtraFileTypes which would  
                          // have given a non-null arg.  
    }  
  
    /** Accepts URLs as well. */  
    private String getPath(String arg) {  
        if (null != arg) {
			File file = new File(arg);
            if (0 == arg.indexOf("http://")  
             || file.exists()) {
				 if (file.isDirectory())
					 return arg;
				 return file.getParent().toString();
			 }				 
        }  
        // else, ask:  
        DirectoryChooser directory = new DirectoryChooser("Select Directory that contains Phase and Intensity folders");
		String dir = directory.getDirectory();  
        if (null == dir) return null; // dialog was canceled  
        dir = dir.replace('\\', '/'); // Windows safe  
        if (!dir.endsWith("/")) dir += "/";  
        return dir;  
    }  
  
    /** Opens URLs as well. */  
    private InputStream open(String path) throws Exception {  
        if (0 == path.indexOf("http://"))  
            return new java.net.URL(path).openStream();  
        return new FileInputStream(path);  
    }  
  
    private boolean parse(String path) {  
        // Open timestamp file
		//File timestamps_txt = new File(path);
		//File working_dir = timestamps_txt.getParentFile();
		/*
		Vector<String[]> timestamps;
		if (timestamps_txt.canRead()){
			 timestamps = readTextFile(timestamps_txt);	
		}
		else {
			IJ.showMessage("timestamps.txt file not found  in :\n \n"+path);
			return false;
		}
		*/
        
		//File intensity_bin = new File(new File(new File(working_dir,"Intensity"),"Float"),"Bin");
		//File phase_bin = new File(new File(new File(working_dir,"Phase"),"Float"),"Bin");
		//IJ.freeMomory();
		FolderOpener opener = new FolderOpener();
		GenericDialog dialog = new GenericDialog("Import Koala sequence");
		dialog.addCheckbox("Open as a virtual stack ", true);
		//dialog.addCheckbox("Conversion to nm ", true);
		dialog.showDialog();
		opener.openAsVirtualStack(dialog.getNextBoolean());
		//boolean convert = dialog.getNextBoolean();
		//ImagePlus phase = new ImagePlus();
		//ImagePlus intensity = new ImagePlus();
		ImagePlus[] data = new ImagePlus[2];
		data[0] = new ImagePlus();
		data[1] = new ImagePlus();
		data[0] = opener.open(path+"/Phase/Float/Bin/");
		data[1] = opener.open(path+"/Intensity/Float/Bin/");
		int length = data[0].getNSlices();
		//ImageStack stack = new ImageStack(phase.getWidth(),phase.getHeight(), 2*phase.getNSlices());
		ImagePlus sequence = new ImagePlus();
		RGBStackMerge merge = new RGBStackMerge();
		sequence = merge.mergeChannels(data,false);
		sequence.setDimensions(2,1,length);
		sequence.setDisplayMode(IJ.GRAYSCALE);
		sequence.setTitle("Koala Sequence");
		sequence.copyScale(data[0]);
		FileInfo FI = sequence.getFileInfo();
		FI.directory = path;
		sequence.setFileInfo(FI);
		sequence.show();
		return true;
    }
	public Vector<String[]> readTextFile(File file) {
		BufferedReader reader = null;
		Vector<String[]> index_data = new Vector<String[]>();
		int index;
		try {
			reader = new BufferedReader(new FileReader(file));
			String text = null;
			index = -1;
			String[] split = new String[100];
			// repeat until all lines is read
			while ((text = reader.readLine()) != null) {
				index++;
				split = text.split(" ");
				index_data.add(index,split);
			}
				
			
		} catch (FileNotFoundException e){
			IJ.handleException(e);
		} catch (IOException e){
			IJ.handleException(e);
		} finally {
			try {
				if(reader != null){
					reader.close();
				}
			} catch (IOException e){
				IJ.handleException(e);
			}
		}
		return index_data;
	}
}  
