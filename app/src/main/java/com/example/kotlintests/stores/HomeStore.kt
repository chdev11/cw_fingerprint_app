package com.example.kotlintests.stores

import android.app.Application
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.lifecycle.AndroidViewModel
import com.rscja.deviceapi.FingerprintWithFIPS
import com.rscja.deviceapi.FingerprintWithFIPS.GRABCallBack
import com.rscja.deviceapi.FingerprintWithFIPS.IdentificationCallBack
import com.rscja.deviceapi.FingerprintWithFIPS.PtCaptureCallBack
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File
import androidx.core.net.toUri

class HomeStore(application: Application) : AndroidViewModel(application), GRABCallBack,
    PtCaptureCallBack, IdentificationCallBack {

    private var mFingerprint: FingerprintWithFIPS? = null

    private val _fingerprintCount = MutableStateFlow<Int?>(0)
    val fingerprintCount: StateFlow<Int?> = _fingerprintCount.asStateFlow()

    private val _messageList = MutableStateFlow<List<Pair<String, MessageType>>>(emptyList())
    val messageList: StateFlow<List<Pair<String, MessageType>>> = _messageList.asStateFlow()

    private val _fingerprint = MutableStateFlow<ByteArray?>(null)
    val fingerprint: StateFlow<ByteArray?> = _fingerprint.asStateFlow()

    private val _fingerprintPath = MutableStateFlow<String?>(null)
    val fingerprintPath: StateFlow<String?> = _fingerprintPath.asStateFlow()

    init {
        configure()
    }

    private fun configure() {
        try {
            mFingerprint = FingerprintWithFIPS.getInstance()
            mFingerprint?.init()
            mFingerprint?.setPtCaptureCallBack(this)
            mFingerprint?.setGrabCallBack(this)
            mFingerprint?.setIdentificationCallBack(this)

            _fingerprintCount.value = mFingerprint?.fingersCount
        } catch (e: Exception) {
            addMessage("Erro ao inicializar biometria: ${e.message}", MessageType.ERROR)
        }
    }

    fun addMessage(msg: String, type: MessageType) {
        if (_messageList.value.isEmpty() || _messageList.value.first().first != msg) {
            _messageList.value = listOf(Pair(msg, type)) + _messageList.value
        }
    }

    fun clearMessageList() {
        _messageList.value = emptyList()
    }

    var lastAction = -1

    fun capture() {
        lastAction = 0
        mFingerprint?.startPtCapture()
    }

    fun grab() {
        lastAction = 1
        mFingerprint?.startGRAB()
    }

    fun identify() {
        mFingerprint?.startIdentification()
    }

    fun deleteFingers() {
        mFingerprint?.deleteAllFingers()
        _fingerprintCount.value = mFingerprint?.fingersCount

        addMessage("Digitais apagadas!", MessageType.SUCCESS)
    }

    override fun messageInfo(p0: String?) {
        p0?.let {
            addMessage("Info: $it", MessageType.INFO)
        }
    }

    override fun onComplete(p0: Boolean, p1: Int, p2: Int) {
        if (p0) {
            addMessage("Digital validada com sucesso!", MessageType.SUCCESS)
        } else {
            addMessage("Digital não cadastrada.", MessageType.ERROR)
        }
    }

    override fun onComplete(p0: Boolean, p1: ByteArray?, p2: Int) {
        if (p0) {
            p1?.let {
                if (lastAction == 1) {
                    try {
                        val folder = File(
                            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                            "Fingerprint"
                        )
                        if (!folder.exists()) folder.mkdirs()

                        val filename = "fingerprint_${System.currentTimeMillis()}.png"
                        val file = File(folder, filename)
                        val path = file.absolutePath

                        mFingerprint?.generateImg(it, path)

                        _fingerprintPath.value = path
                        addMessage("Imagem salva em: $path", MessageType.SUCCESS)
                    } catch (e: Exception) {
                        addMessage("Erro ao salvar imagem: ${e.message}", MessageType.ERROR)
                    }
                }

                _fingerprint.value = it

                if (lastAction == 0) mFingerprint?.storeFinger(it)

                _fingerprintCount.value = mFingerprint?.fingersCount

                addMessage("Digital capturada com sucesso!", MessageType.SUCCESS)
            }
        } else {
            addMessage("Falha na captura da digital.", MessageType.ERROR)
        }

        lastAction = -1
    }

    override fun progress(p0: Int) {
        val currentMessages = _messageList.value.toMutableList()
        val filteredMessages = currentMessages.filter { it.second != MessageType.PERCENTAGE }
        val newMessage = Pair(p0.toString(), MessageType.PERCENTAGE)
        _messageList.value = filteredMessages + newMessage
    }

    private fun createImageFilePathViaMediaStore(filename: String): String? {
        val context = getApplication<Application>()
        val resolver = context.contentResolver

        val pictures = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
        val folder = File(pictures, "Fingerprint")

        if (!folder.exists()) folder.mkdirs()

        val file = File(folder, filename)
        val physicalPath = file.absolutePath

        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, filename)
            put(MediaStore.Images.Media.MIME_TYPE, "image/png")
            put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/Fingerprint")
            put(MediaStore.Images.Media.IS_PENDING, 0)
        }

        resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)

        return physicalPath
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun openFingerprintFolder(context: Context) {
        val base = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
        val folder = File(base, "Fingerprint")
        folder.mkdirs()

        val folderUri =
            "content://com.android.externalstorage.documents/document/primary%3APictures%2FFingerprint".toUri()

        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE).apply {
            putExtra(DocumentsContract.EXTRA_INITIAL_URI, folderUri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Não foi possível abrir a pasta.", Toast.LENGTH_SHORT).show()
        }
    }
}

enum class MessageType {
    INFO,
    SUCCESS,
    ERROR,
    PERCENTAGE
}
