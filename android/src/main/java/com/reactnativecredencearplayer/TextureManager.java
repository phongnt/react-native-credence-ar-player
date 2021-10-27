package com.reactnativecredencearplayer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TextureManager {

  private Map<String, TextureHolder> addedTextures = new HashMap<>();

  public enum Type {
    IMAGE,
    COLOR
  }

  public static class TextureHolder {
    String id;
    Type type;
    String filePath;
    String color;
  }

  public void add(TextureHolder textureHolder) {
    addedTextures.put(textureHolder.id, textureHolder);
  }

  public TextureHolder get(String id) {
    return addedTextures.get(id);
  }
}
