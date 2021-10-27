package com.reactnativecredencearplayer;

import android.content.Context;
import android.content.res.AssetManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class ModelManager {

  private Context context;

  public ModelManager(Context context) {
    this.context = context;
  }

  public String findModelFilePath(String modelName) {
    File modelDirectory = new File(context.getFilesDir(), modelName);
    if (!modelDirectory.exists() || !modelDirectory.isDirectory()) return null;
    for (String filename : modelDirectory.list()) {
      if (filename.endsWith(".gltf")) return new File(modelDirectory, filename).getAbsolutePath();
    }
    return null;
  }

  public String findModelDirectory(String modelName) {
    File modelDirectory = new File(context.getFilesDir(), modelName);
    return modelDirectory.getAbsolutePath();
  }

  public boolean checkModelExist(String modelName) {
    File modelDirectory = new File(context.getFilesDir(), modelName);
    return modelDirectory.exists() && modelDirectory.isDirectory();
  }

  public void downloadFromAsset(String modelName) throws IOException {
    File modelDirectory = new File(context.getFilesDir(), modelName);
    modelDirectory.mkdirs();
    copyDirOrFileFromAssetManager("models/" + modelName, modelDirectory.getAbsolutePath());
  }

  private void copyDirOrFileFromAssetManager(String arg_assetDir, String arg_destinationDir) throws IOException {
    File dest_dir = new File(arg_destinationDir);

    createDir(dest_dir);

    AssetManager asset_manager = context.getAssets();
    String[] files = asset_manager.list(arg_assetDir);

    for (String file : files) {
      String abs_asset_file_path = addTrailingSlash(arg_assetDir) + file;
      String[] sub_files = asset_manager.list(abs_asset_file_path);

      if (sub_files.length == 0) {
        // It is a file
        String dest_file_path = addTrailingSlash(arg_destinationDir) + file;
        copyAssetFile(abs_asset_file_path, dest_file_path);
      } else {
        // It is a sub directory
        copyDirOrFileFromAssetManager(abs_asset_file_path, addTrailingSlash(arg_destinationDir) + file);
      }
    }
  }

  private void copyAssetFile(String assetFilePath, String destinationFilePath) throws IOException {
    InputStream in = context.getAssets().open(assetFilePath);
    OutputStream out = new FileOutputStream(destinationFilePath);

    byte[] buf = new byte[1024];
    int len;
    while ((len = in.read(buf)) > 0)
      out.write(buf, 0, len);
    in.close();
    out.close();
  }

  private String addTrailingSlash(String path) {
    if (path.charAt(path.length() - 1) != '/') {
      path += "/";
    }
    return path;
  }

  private String addLeadingSlash(String path) {
    if (path.charAt(0) != '/') {
      path = "/" + path;
    }
    return path;
  }

  private void createDir(File dir) throws IOException {
    if (dir.exists()) {
      if (!dir.isDirectory()) {
        throw new IOException("Can't create directory, a file is in the way");
      }
    } else {
      dir.mkdirs();
      if (!dir.isDirectory()) {
        throw new IOException("Unable to create directory");
      }
    }
  }

}
