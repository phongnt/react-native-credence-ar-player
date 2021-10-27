package com.reactnativecredencearplayer;

import android.content.Context;
import android.util.Log;
import android.view.Choreographer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.credence.MainFragment;
import com.facebook.react.ReactActivity;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.common.MapBuilder;
import com.facebook.react.uimanager.ThemedReactContext;
import com.facebook.react.uimanager.ViewGroupManager;
import com.facebook.react.uimanager.events.RCTEventEmitter;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class CredenceArPlayerViewManager extends ViewGroupManager<FrameLayout> {

  public static final String REACT_CLASS = "CredenceArPlayerView";

  private ThemedReactContext context;
  private FrameLayout rootView;
  private MainFragment fragment;
  private ModelManager modelManager;
  private TextureManager textureManager;

  private final Map<String, String> placedObjectVariant = new HashMap<>();
  private String selectedObjectId = null;
  private boolean firstAdd = true;

  // Receive actions from Ar player fragment, send them to react native part.
  private MainFragment.ActionEvent actionEvent = new MainFragment.ActionEvent() {
    @Override
    public void onSelected(String objectId, String productId) {
      selectedObjectId = objectId;
      WritableMap event = Arguments.createMap();
      event.putString("productId", productId);
      event.putString("objectId", objectId);
      if (placedObjectVariant.containsKey(objectId)) {
        event.putString("variantId", placedObjectVariant.get(objectId));
      }
      context.getJSModule(RCTEventEmitter.class)
        .receiveEvent(rootView.getId(), "onSelected", event);
    }

    @Override
    public void onSelectedAction(String productId, MainFragment.SelectedAction action) {
      WritableMap event = Arguments.createMap();
      event.putString("action", action.name());
      context.getJSModule(RCTEventEmitter.class)
        .receiveEvent(rootView.getId(), "onAction", event);
    }

    @Override
    public void onClosed() {
      removeFragment();
      context.getJSModule(RCTEventEmitter.class)
        .receiveEvent(rootView.getId(), "onClosed", null);
    }

    @Override
    public void onAdd() {
      context.getJSModule(RCTEventEmitter.class)
        .receiveEvent(rootView.getId(), "onAddProduct", null);
    }

    @Override
    public void onDetails() {
      context.getJSModule(RCTEventEmitter.class)
        .receiveEvent(rootView.getId(), "onShowDetail", null);
    }
  };

  @Override
  @NonNull
  public String getName() {
    return REACT_CLASS;
  }

  @Override
  @NonNull
  public FrameLayout createViewInstance(ThemedReactContext reactContext) {
    Log.d(CredenceArPlayerViewManager.class.getName(), "createViewInstance");
    context = reactContext;
    this.modelManager = new ModelManager(context);
    this.textureManager = new TextureManager();

    LayoutInflater inflater = (LayoutInflater) reactContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    rootView = (FrameLayout) inflater.inflate(R.layout.fragment_container, null);
    return rootView;
  }

  @Nullable @Override
  public Map<String, Object> getExportedCustomDirectEventTypeConstants() {
    return MapBuilder.<String, Object>builder()
      .put("onSelected",
        MapBuilder.of("registrationName", "onSelected"))
      .put("onAction",
        MapBuilder.of("registrationName", "onAction"))
      .put("onAddProduct",
        MapBuilder.of("registrationName", "onAddProduct"))
      .put("onShowDetail",
        MapBuilder.of("registrationName", "onShowDetail"))
      .put("onClosed",
        MapBuilder.of("registrationName", "onClosed"))
      .build();
  }

  @Override
  public void receiveCommand(@NonNull FrameLayout root, String command, @Nullable ReadableArray args) {
    super.receiveCommand(root, command, args);
    Log.d(CredenceArPlayerViewManager.class.getName(), "receiveCommand = " + command);
    switch (command) {
      case "create":
        firstAdd = true;
        int reactNativeViewId = args.getInt(0);
        root.postDelayed(() -> {
          setLayoutHack(root);
          createFragment(context, reactNativeViewId);
        }, 2000);
        break;
      case "destroy":
        removeFragment();
        break;
      case "add":
        String productId = args.getString(0);
        String objectId = args.getString(2);
        String model = args.getMap(1).getString("name");
        Double scale = args.getMap(1).getDouble("scale");
        if (!modelManager.checkModelExist(model)) {
          try {
            modelManager.downloadFromAsset(model);
          } catch (IOException e) {
            e.printStackTrace();
            break;
          }
        }
        ReadableArray textureArray = args.getMap(1).getArray("textures");
        if (textureArray != null) {
          for (int i = 0; i < textureArray.size(); i++) {
            ReadableMap texture = textureArray.getMap(i);
            TextureManager.TextureHolder textureHolder = new TextureManager.TextureHolder();
            textureHolder.id = texture.getString("id");
            textureHolder.type = texture.getString("type").contentEquals("image") ?
              TextureManager.Type.IMAGE : TextureManager.Type.COLOR;
            if (textureHolder.type == TextureManager.Type.IMAGE) {
              textureHolder.filePath = new File(
                modelManager.findModelDirectory(model),
                texture.getString("file")).getAbsolutePath();
            } else {
              textureHolder.color = texture.getString("color");
            }
            textureManager.add(textureHolder);
          }
        }
        String modelFilename = modelManager.findModelFilePath(model);
        root.postDelayed(() -> {
          if (fragment != null) {
            fragment.addObject(productId, modelFilename, scale, objectId);
            firstAdd = false;
          }
        }, firstAdd ? 4000 : 1000);
        break;
      case "remove":
        String objectIdToRemove = args.getString(0);
        fragment.removeObject(objectIdToRemove);
        break;
      case "clear":
        fragment.clearObject();
        break;
      case "select":
        String product = args.getString(0);
        String variantId = args.getString(1);
        String textureId = args.getString(2);

        TextureManager.TextureHolder textureHolder = textureManager.get(textureId);
        if (textureHolder.type == TextureManager.Type.IMAGE) {
          fragment.setTexture(textureHolder.filePath);
        }
        if (selectedObjectId != null) {
          placedObjectVariant.put(selectedObjectId, variantId);
        }
        break;
    }
  }

  private void createFragment(ReactContext context, int id) {
    if (context.getCurrentActivity().isFinishing() ||
      context.getCurrentActivity().isDestroyed() ||
      context.getCurrentActivity().findViewById(id) == null) {
      return;
    }
    if (fragment != null) removeFragment();
    fragment = MainFragment.newInstance(actionEvent);
    ((ReactActivity) context.getCurrentActivity()).getSupportFragmentManager()
      .beginTransaction()
      .replace(id, fragment, String.valueOf(id))
      .commit();
  }

  private void removeFragment() {
    if (context.getCurrentActivity() == null || fragment == null) return;
    ((ReactActivity) context.getCurrentActivity()).getSupportFragmentManager()
      .beginTransaction().remove(fragment).commitNowAllowingStateLoss();
    fragment = null;
  }

  private void setLayoutHack(ViewGroup view) {
    Choreographer.getInstance().postFrameCallback(new Choreographer.FrameCallback() {
      @Override
      public void doFrame(long l) {
        manuallyLayoutChildren(view);
        view.getViewTreeObserver().dispatchOnGlobalLayout();
        Choreographer.getInstance().postFrameCallback(this);
      }
    });
  }

  private void manuallyLayoutChildren(ViewGroup view) {
    for (int i = 0; i < view.getChildCount(); i++) {
      View child = view.getChildAt(i);
      child.measure(
        View.MeasureSpec.makeMeasureSpec(view.getMeasuredWidth(),
          View.MeasureSpec.EXACTLY),
        View.MeasureSpec.makeMeasureSpec(view.getMeasuredHeight(),
          View.MeasureSpec.EXACTLY)
      );
      child.layout(0, 0, child.getMeasuredWidth(), child.getMeasuredHeight());
    }
  }

//  @ReactProp(name = "color")
//  public void setColor(View view, String color) {
//    view.setBackgroundColor(Color.parseColor(color));
//  }

}
