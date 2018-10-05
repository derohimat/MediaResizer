package pyxis.uzuki.live.mediaresizersample.activity

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.support.v7.app.AppCompatActivity
import android.view.View
import kotlinx.android.synthetic.main.activity_demo.*
import pyxis.uzuki.live.mediaresizer.MediaResizer
import pyxis.uzuki.live.mediaresizer.data.ImageResizeOption
import pyxis.uzuki.live.mediaresizer.data.ResizeOption
import pyxis.uzuki.live.mediaresizer.data.VideoResizeOption
import pyxis.uzuki.live.mediaresizer.model.ImageMode
import pyxis.uzuki.live.mediaresizer.model.MediaType
import pyxis.uzuki.live.mediaresizer.model.ScanRequest
import pyxis.uzuki.live.mediaresizer.model.VideoCompressQuality
import pyxis.uzuki.live.mediaresizersample.R
import pyxis.uzuki.live.mediaresizersample.utils.displayImageResult
import pyxis.uzuki.live.mediaresizersample.utils.displayVideoResult
import pyxis.uzuki.live.richutilskt.utils.*
import java.io.File

class KotlinActivity : AppCompatActivity() {

    private var originVideoPath: String? = null
    private var resultVideoPath: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_demo)

        btnCamera.setOnClickListener {
            RPickMedia.instance.pickFromCamera(this) { code, path -> resultProcess(code, path, MediaType.IMAGE) }
        }

        btnVideo.setOnClickListener {
            RPickMedia.instance.pickFromVideoCamera(this) { code, path -> resultProcess(code, path, MediaType.VIDEO) }
        }

        btnGallery.setOnClickListener {
            RPickMedia.instance.pickFromGallery(this) { code, path -> resultProcess(code, path, MediaType.IMAGE) }
        }

        btnVideoGallery.setOnClickListener {
            RPickMedia.instance.pickFromVideo(this) { code, path -> resultProcess(code, path, MediaType.VIDEO) }
        }

        btnPlayOrigin.setOnClickListener {
            if (originVideoPath != null) {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(originVideoPath))
                intent.setDataAndType(Uri.parse(originVideoPath), "video/mp4")
                startActivity(intent)
            }
        }

        btnPlayResult.setOnClickListener {
            if (resultVideoPath != null) {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(resultVideoPath))
                intent.setDataAndType(Uri.parse(resultVideoPath), "video/mp4")
                startActivity(intent)
            }
        }
    }

    private fun resultProcess(code: Int, path: String, type: MediaType) {
        if (code == RPickMedia.PICK_FAILED) {
            return
        }

        if (type == MediaType.VIDEO) {
            selectVideoStrategy(path)
        } else {
            processImage(path)
        }
    }

    private fun processImage(path: String) {
        val file = "${Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)}/MediaResizer/".toFile()
        file.mkdirs()
        val imageFile = File(file, "${System.currentTimeMillis().asDateString("yyyy-MM-dd HH:mm:ss")}.jpg")
        val progress = progress("Encoding...")

        val resizeOption = ImageResizeOption.Builder()
                .setImageProcessMode(ImageMode.ResizeAndCompress)
                .setImageResolution(1280, 720)
                .setBitmapFilter(false)
                .setCompressFormat(Bitmap.CompressFormat.JPEG)
                .setCompressQuality(75)
                .setScanRequest(ScanRequest.TRUE)
                .build()

        val option = ResizeOption.Builder()
                .setMediaType(MediaType.IMAGE)
                .setImageResizeOption(resizeOption)
                .setTargetPath(path)
                .setOutputPath(imageFile.absolutePath)
                .setCallback { code, output ->
                    txtStatus.text = displayImageResult(code, path, output)
                    progress.dismiss()
                }
                .build()

        MediaResizer.process(option)
    }

    private fun selectVideoStrategy(path: String) {
        val lists = arrayListOf("Low", "Medium", "High")
        selector(lists, { dialog, item, position ->
            val quality: VideoCompressQuality = when (position) {
                0 -> VideoCompressQuality.LOW
                1 -> VideoCompressQuality.MEDIUM
                else -> VideoCompressQuality.HIGH
            }
            processVideo(path, quality)
            dialog.dismiss()
        })
    }

    private fun processVideo(path: String, quality: VideoCompressQuality) {
        originVideoPath = path
        val file = "${Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)}/MediaResizer/".toFile()
        file.mkdirs()
        val imageFile = File(file, "${System.currentTimeMillis().asDateString("yyyy-MM-dd HH:mm:ss")}.mp4")
        val progress = progress("Encoding...")

        val resizeOption = VideoResizeOption.Builder()
                .setQuality(path, quality)
                .setScanRequest(ScanRequest.TRUE)
                .build()

        val option = ResizeOption.Builder()
                .setMediaType(MediaType.VIDEO)
                .setVideoResizeOption(resizeOption)
                .setTargetPath(path)
                .setOutputPath(imageFile.absolutePath)
                .setCallback { code, output ->
                    resultVideoPath = output
                    lyVideo.visibility = View.VISIBLE
                    txtStatus.text = displayVideoResult(code, path, output)
                    progress.dismiss()
                }
                .build()

        MediaResizer.process(option)
    }
}
