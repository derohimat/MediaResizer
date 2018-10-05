package pyxis.uzuki.live.mediaresizersample.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.File;
import java.util.Arrays;

import pyxis.uzuki.live.mediaresizer.MediaResizer;
import pyxis.uzuki.live.mediaresizer.data.ImageResizeOption;
import pyxis.uzuki.live.mediaresizer.data.ResizeOption;
import pyxis.uzuki.live.mediaresizer.data.VideoResizeOption;
import pyxis.uzuki.live.mediaresizer.model.ImageMode;
import pyxis.uzuki.live.mediaresizer.model.MediaType;
import pyxis.uzuki.live.mediaresizer.model.ScanRequest;
import pyxis.uzuki.live.mediaresizer.model.VideoCompressQuality;
import pyxis.uzuki.live.mediaresizersample.R;
import pyxis.uzuki.live.mediaresizersample.utils.ResultBuilder;
import pyxis.uzuki.live.pyxinjector.annotation.BindView;
import pyxis.uzuki.live.pyxinjector.annotation.OnClick;
import pyxis.uzuki.live.pyxinjector.base.InjectActivity;
import pyxis.uzuki.live.richutilskt.utils.RPickMedia;
import pyxis.uzuki.live.richutilskt.utils.RichUtils;

/**
 * MediaResizer
 * Class: JavaActivity
 * Created by Pyxis on 2017-11-23.
 * <p>
 * Description:
 */

public class JavaActivity extends InjectActivity {
    private @BindView TextView txtStatus;
    private @BindView LinearLayout lyVideo;
    private String originVideoPath, resultVideoPath;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_demo);
    }

    @OnClick(R.id.btnCamera)
    private void clickCamera() {
        RPickMedia.instance.pickFromCamera(this, (code, path) -> {
            resultProcess(code, path, MediaType.IMAGE);
        });
    }

    @OnClick(R.id.btnVideo)
    private void clickVideo() {
        RPickMedia.instance.pickFromVideoCamera(this, (code, path) -> {
            resultProcess(code, path, MediaType.VIDEO);
        });
    }

    @OnClick(R.id.btnGallery)
    private void clickGallery() {
        RPickMedia.instance.pickFromGallery(this, (code, path) -> {
            resultProcess(code, path, MediaType.IMAGE);
        });
    }

    @OnClick(R.id.btnVideoGallery)
    private void clickVideoGallery() {
        RPickMedia.instance.pickFromVideo(this, (code, path) -> {
            resultProcess(code, path, MediaType.VIDEO);
        });
    }

    @OnClick(R.id.btnPlayOrigin)
    private void clickPlayOrigin() {
        if (originVideoPath != null) {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(originVideoPath));
            intent.setDataAndType(Uri.parse(originVideoPath), "video/mp4");
            startActivity(intent);
        }
    }

    @OnClick(R.id.btnPlayResult)
    private void clickPlayResult() {
        if (resultVideoPath != null) {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(resultVideoPath));
            intent.setDataAndType(Uri.parse(resultVideoPath), "video/mp4");
            startActivity(intent);
        }
    }

    private void resultProcess(int code, String path, MediaType type) {
        if (code == RPickMedia.PICK_FAILED)
            return;

        if (type == MediaType.VIDEO) {
            selectVideoStrategy(path);
        } else {
            processImage(path);
        }
    }

    private void processImage(String path) {
        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + "/MediaResizer/");
        file.mkdirs();

        File imageFile = new File(file, RichUtils.asDateString(System.currentTimeMillis(), "yyyy-MM-dd-HH:mm:ss") + ".jpg");
        DialogInterface progress = RichUtils.progress(this, "Encoding...");

        ImageResizeOption resizeOption = new ImageResizeOption.Builder()
                .setImageProcessMode(ImageMode.ResizeAndCompress)
                .setImageResolution(1280, 720)
                .setBitmapFilter(false)
                .setCompressFormat(Bitmap.CompressFormat.JPEG)
                .setCompressQuality(75)
                .setScanRequest(ScanRequest.TRUE)
                .build();

        ResizeOption option = new ResizeOption.Builder()
                .setMediaType(MediaType.IMAGE)
                .setImageResizeOption(resizeOption)
                .setTargetPath(path)
                .setOutputPath(imageFile.getAbsolutePath())
                .setCallback((code, output) -> {
                    txtStatus.setText(ResultBuilder.displayImageResult(code, path, output));
                    progress.dismiss();
                }).build();

        MediaResizer.process(option);
    }

    private void selectVideoStrategy(String path) {
        String[] arrays = new String[]{"Low", "Medium", "High"};
        RichUtils.selector(this, Arrays.asList(arrays), (dialog, item, position) -> {
            VideoCompressQuality quality;
            switch (position) {
                case 0:
                    quality = VideoCompressQuality.LOW;
                    break;
                default:
                case 1:
                    quality = VideoCompressQuality.MEDIUM;
                    break;
                case 2:
                    quality = VideoCompressQuality.HIGH;
                    break;
            }

            processVideo(path, quality);
            dialog.dismiss();
        });
    }

    private void processVideo(String path, VideoCompressQuality quality) {
        originVideoPath = path;
        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + "/MediaResizer/");
        file.mkdirs();

        File imageFile = new File(file, RichUtils.asDateString(System.currentTimeMillis(), "yyyyMMdd_HHmmss") + ".mp4");
        DialogInterface progress = RichUtils.progress(this, "Encoding...");

        VideoResizeOption resizeOption = new VideoResizeOption.Builder()
                .setQuality(path, quality)
                .setScanRequest(ScanRequest.TRUE)
                .build();

        ResizeOption option = new ResizeOption.Builder()
                .setMediaType(MediaType.VIDEO)
                .setVideoResizeOption(resizeOption)
                .setTargetPath(path)
                .setOutputPath(imageFile.getAbsolutePath())
                .setCallback((code, output) -> {
                    lyVideo.setVisibility(View.VISIBLE);
                    resultVideoPath = output;
                    txtStatus.setText(ResultBuilder.displayVideoResult(code, path, output));
                    progress.dismiss();
                }).build();

        MediaResizer.process(option);
    }
}
