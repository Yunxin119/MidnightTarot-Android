package com.yunxin.midnighttarotai.share;

import android.app.Dialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.yunxin.midnighttarotai.R;
import com.yunxin.midnighttarotai.payment.DailyReadingManager;
import com.yunxin.midnighttarotai.payment.PaymentManager;
import com.yunxin.midnighttarotai.result.ResultActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

import javax.annotation.Nullable;

public class SharePreviewDialog extends BottomSheetDialogFragment {
    private Bitmap previewBitmap;
    private ImageView previewImage;
    private MaterialButton saveButton;
    private MaterialButton shareButton;
    private DailyReadingManager dailyReadingManager;
    private PaymentManager paymentManager;
    public static SharePreviewDialog newInstance(Bitmap bitmap) {
        SharePreviewDialog dialog = new SharePreviewDialog();
        dialog.previewBitmap = bitmap;
        return dialog;
    }

    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        BottomSheetDialog dialog = new BottomSheetDialog(requireContext(), R.style.AppTheme_BottomSheetDialogTheme);
        dialog.setOnShowListener(dialogInterface -> {
            BottomSheetDialog bottomSheetDialog = (BottomSheetDialog) dialogInterface;
            View bottomSheetInternal = bottomSheetDialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
            if (bottomSheetInternal != null) {
                BottomSheetBehavior.from(bottomSheetInternal).setState(BottomSheetBehavior.STATE_EXPANDED);
                BottomSheetBehavior.from(bottomSheetInternal).setSkipCollapsed(true);
            }
        });

        return dialog;
    }

    @Override
    public void onCreate(@androidx.annotation.Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dailyReadingManager = new DailyReadingManager(requireContext());
        paymentManager = new PaymentManager(requireContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_share_preview, container, false);

        previewImage = view.findViewById(R.id.previewImage);
        saveButton = view.findViewById(R.id.saveButton);
        shareButton = view.findViewById(R.id.shareButton);

        previewImage.setImageBitmap(previewBitmap);

        saveButton.setOnClickListener(v -> saveImage());
        shareButton.setOnClickListener(v -> shareImage());

        return view;
    }

    public void setDailyReadingManager(DailyReadingManager manager) {
        this.dailyReadingManager = manager;
    }

    public void saveImage() {
        if (previewImage == null) {
            return;
        }
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ContentValues values = new ContentValues();
                values.put(MediaStore.Images.Media.DISPLAY_NAME, "tarot_reading_" +
                        System.currentTimeMillis() + ".jpg");
                values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
                values.put(MediaStore.Images.Media.RELATIVE_PATH,
                        Environment.DIRECTORY_PICTURES + "/TarotAI");

                ContentResolver resolver = requireContext().getContentResolver();
                Uri imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

                if (imageUri != null) {
                    try (OutputStream outputStream = resolver.openOutputStream(imageUri)) {
                        previewBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
                        Toast.makeText(getContext(), "Image saved successfully",
                                Toast.LENGTH_SHORT).show();
                    }
                }
            } else {
                File imagesDir = Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_PICTURES); // Get mobile's image folder
                File tarotDir = new File(imagesDir, "MidnightTarotAI"); // Find the target folder
                if (!tarotDir.exists()) {
                    tarotDir.mkdirs();
                }

                File imageFile = new File(tarotDir, "tarot_reading_" +
                        System.currentTimeMillis() + ".jpg"); // Create a new file
                try (FileOutputStream fos = new FileOutputStream(imageFile)) {
                    previewBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                    Toast.makeText(getContext(), "Image saved successfully",
                            Toast.LENGTH_SHORT).show();
                }

                MediaScannerConnection.scanFile(getContext(),
                        new String[]{imageFile.getAbsolutePath()},
                        new String[]{"image/jpeg"}, null);
            }
        } catch (Exception e) {
            Log.e("SharePreviewDialog", e.toString());
            Toast.makeText(getContext(), "Failed to save image", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Share Image to other platform
     * Create a cache repository 'shares', and create a 'midnight_tarot_ai.jpg' file
     * Use FileProvider to generate an Uri for sharing the file
     * Start sharing intent ACTION_SEND. Grant point to user if applicable
     */
    public void shareImage() {
        if (previewImage == null || dailyReadingManager == null) return;
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) return;
        String userId = currentUser.getUid();

        try {
            // 1. 创建缓存目录
            File cachePath = new File(requireContext().getCacheDir(), "shares");
            cachePath.mkdirs();

            // 2. 创建图片文件
            File imageFile = new File(cachePath, "midnight_tarot_ai_share.jpg");
            try (FileOutputStream stream = new FileOutputStream(imageFile)) {
                previewBitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
            }

            // 3. 获取 URI
            Uri contentUri = FileProvider.getUriForFile(
                    requireContext(),
                    requireContext().getPackageName() + ".fileprovider",
                    imageFile
            );

            // 4. 创建分享 Intent
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("image/*");
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            shareIntent.putExtra(Intent.EXTRA_STREAM, contentUri);

            // 添加分享文本
            String shareText = ((ResultActivity)requireActivity()).buildShareText();
            shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);

            // 5. 检查是否能获得分享积分
            boolean canEarnShareCredit = dailyReadingManager.canEarnShareCredit(userId);

            // 6. 启动分享
            Intent chooserIntent = Intent.createChooser(shareIntent, "Share your reading");
            startActivity(chooserIntent);

            // 7. 处理积分奖励
            if (canEarnShareCredit) {
                paymentManager.addCredits(userId, 1);
                Toast.makeText(requireContext(),
                        "You earned 1 credit for sharing! You can earn another credit tomorrow.",
                        Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            e.printStackTrace();  // 添加详细日志
            Log.e("SharePreviewDialog", "Share failed: " + e.getMessage());
            Toast.makeText(getContext(), "Failed to share image", Toast.LENGTH_SHORT).show();
        }
    }
}
