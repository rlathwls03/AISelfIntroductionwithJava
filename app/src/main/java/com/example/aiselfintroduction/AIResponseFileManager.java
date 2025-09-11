package com.example.aiselfintroduction;

import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * AI 임시 응답 파일 관리 클래스
 * XML 파일로 저장된 AI 응답을 관리
 */
public class AIResponseFileManager {
    private static final String TAG = "AIResponseFileManager";

    // 임시 파일 이름 상수
    public static final String FILE_ROLE = "AI_직무역량.xml";
    public static final String FILE_GOAL = "AI_입사후포부.xml";
    public static final String FILE_REASON = "AI_지원동기.xml";
    public static final String FILE_PERSONALITY = "AI_성격장단점.xml";

    public static final String FILE_AISTATE = "AIStatePrefs.xml";

    // 파일 매핑
    private static final Map<String, String> FILE_MAPPING = new HashMap<>();
    static {
        FILE_MAPPING.put("직무역량", FILE_ROLE);
        FILE_MAPPING.put("입사후포부", FILE_GOAL);
        FILE_MAPPING.put("지원동기", FILE_REASON);
        FILE_MAPPING.put("성격장단점", FILE_PERSONALITY);
    }

    /**
     * 모든 AI 응답 파일이 존재하는지 확인
     * @param context 컨텍스트
     * @return 모든 파일이 존재하면 true, 아니면 false
     */
    public static boolean allFilesExist(Context context) {
        boolean allExist = true;
        for (String fileName : FILE_MAPPING.values()) {
            File file = new File(context.getFilesDir(), fileName);
            if (!file.exists()) {
                allExist = false;
                Log.d(TAG, "누락된 파일: " + fileName);
            }
        }
        return allExist;
    }

    /**
     * 특정 섹션의 파일이 존재하는지 확인
     * @param context 컨텍스트
     * @param section 섹션 키 (직무역량, 입사후포부 등)
     * @return 파일이 존재하면 true, 아니면 false
     */
    public static boolean sectionFileExists(Context context, String section) {
        if (FILE_MAPPING.containsKey(section)) {
            File file = new File(context.getFilesDir(), FILE_MAPPING.get(section));
            return file.exists();
        }
        return false;
    }

    /**
     * AI 응답 파일 내용 읽기
     * @param context 컨텍스트
     * @param section 섹션 키 (직무역량, 입사후포부 등)
     * @return 파일 내용 (XML 형식)
     */
    public static String readResponseFile(Context context, String section) {
        if (!FILE_MAPPING.containsKey(section)) {
            Log.e(TAG, "알 수 없는 섹션: " + section);
            return "";
        }

        String fileName = FILE_MAPPING.get(section);
        StringBuilder content = new StringBuilder();

        try {
            FileInputStream fis = context.openFileInput(fileName);
            InputStreamReader isr = new InputStreamReader(fis, StandardCharsets.UTF_8);
            BufferedReader reader = new BufferedReader(isr);

            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }

            reader.close();
            isr.close();
            fis.close();

            // XML 태그 안의 내용만 추출 (필요한 경우)
            String xmlContent = content.toString();
            // XML 파싱을 여기서 할 수도 있음

            return xmlContent;

        } catch (Exception e) {
            Log.e(TAG, "파일 읽기 오류: " + fileName, e);
            return "";
        }
    }

    /**
     * 모든 AI 응답 파일 삭제 (개선된 버전)
     * @param context 컨텍스트
     * @return 모든 파일 삭제 성공 시 true
     */
    public static boolean deleteAllResponseFiles(Context context) {
        boolean allDeleted = true;

        // 로그 시작
        Log.d(TAG, "=== XML 파일 삭제 시작 ===");

        // 내부 저장소 경로
        File filesDir = context.getFilesDir();
        Log.d(TAG, "내부 저장소 경로: " + filesDir.getAbsolutePath());

        // 파일 삭제 시도
        for (String fileName : FILE_MAPPING.values()) {
            File file = new File(filesDir, fileName);

            // 파일 이미 존재하는지 확인
            if (file.exists()) {
                Log.d(TAG, "파일 발견: " + fileName + ", 크기: " + file.length() + "바이트");

                // 두 가지 방법으로 삭제 시도
                boolean deleted = false;

                // 1. File.delete() 메서드 사용
                deleted = file.delete();

                // 2. 실패 시 Context.deleteFile() 메서드 사용
                if (!deleted) {
                    Log.d(TAG, "File.delete() 실패, Context.deleteFile() 시도");
                    deleted = context.deleteFile(fileName);
                }

                if (!deleted) {
                    allDeleted = false;
                    Log.e(TAG, "파일 삭제 실패: " + fileName);
                } else {
                    Log.d(TAG, "파일 삭제 성공: " + fileName);
                }
            } else {
                Log.d(TAG, "파일 없음: " + fileName);
            }
        }

        // 삭제 후 파일 존재 여부 다시 확인
        Log.d(TAG, "삭제 후 파일 확인:");
        for (String fileName : FILE_MAPPING.values()) {
            File file = new File(filesDir, fileName);
            Log.d(TAG, "- " + fileName + ": " + (file.exists() ? "여전히 존재함" : "삭제됨"));
        }

        // ✅ shared_prefs 삭제 추가
        File sharedPrefsFile = new File(filesDir.getParent() + "/shared_prefs/AIStatePrefs.xml");
        if (sharedPrefsFile.exists()) {
            boolean deleted = sharedPrefsFile.delete();
            if (!deleted) {
                allDeleted = false;
                Log.e(TAG, "shared_prefs 삭제 실패: AIStatePrefs.xml");
            } else {
                Log.d(TAG, "shared_prefs 삭제 성공: AIStatePrefs.xml");
            }
        }

        Log.d(TAG, "=== XML 파일 삭제 종료 ===");
        return allDeleted;
    }

    /**
     * AI 응답 파일 저장 (테스트용 - 실제로는 AI 팀에서 저장)
     * @param context 컨텍스트
     * @param section 섹션 키 (직무역량, 입사후포부 등)
     * @param content XML 형식의 내용
     * @return 저장 성공 시 true
     */
    public static boolean saveResponseFile(Context context, String section, String content) {
        if (!FILE_MAPPING.containsKey(section)) {
            Log.e(TAG, "알 수 없는 섹션: " + section);
            return false;
        }

        String fileName = FILE_MAPPING.get(section);

        try {
            FileOutputStream fos = context.openFileOutput(fileName, Context.MODE_PRIVATE);
            fos.write(content.getBytes(StandardCharsets.UTF_8));
            fos.close();
            Log.d(TAG, "파일 저장 성공: " + fileName);
            return true;
        } catch (Exception e) {
            Log.e(TAG, "파일 저장 오류: " + fileName, e);
            return false;
        }
    }

    /**
     * XML 내용에서 텍스트 추출 (개선된 버전)
     * @param xmlContent XML 문자열
     * @return 추출된 텍스트
     */
    public static String extractTextFromXml(String xmlContent) {
        if (xmlContent == null || xmlContent.isEmpty()) {
            Log.e(TAG, "XML 내용이 비어있음");
            return "";
        }

        try {
            // CDATA 섹션 추출 시도
            if (xmlContent.contains("<![CDATA[") && xmlContent.contains("]]>")) {
                int startIndex = xmlContent.indexOf("<![CDATA[") + 9;
                int endIndex = xmlContent.indexOf("]]>");
                if (startIndex > 0 && endIndex > startIndex) {
                    String cdataContent = xmlContent.substring(startIndex, endIndex);
                    Log.d(TAG, "CDATA 추출 성공: " + cdataContent.substring(0, Math.min(50, cdataContent.length())) + "...");
                    return cdataContent.trim();
                }
            }

            // content 태그에서 추출 시도
            if (xmlContent.contains("<content>") && xmlContent.contains("</content>")) {
                int startIndex = xmlContent.indexOf("<content>") + 9;
                int endIndex = xmlContent.indexOf("</content>");
                if (startIndex > 0 && endIndex > startIndex) {
                    String contentTagText = xmlContent.substring(startIndex, endIndex);
                    Log.d(TAG, "content 태그 추출 성공: " + contentTagText.substring(0, Math.min(50, contentTagText.length())) + "...");
                    return contentTagText.trim();
                }
            }

            // 일반 XML 태그 제거
            String text = xmlContent.replaceAll("<[^>]*>", " ");
            text = text.replaceAll("\\s+", " ").trim();

            // 특수 문자 디코딩
            text = text.replace("&lt;", "<")
                    .replace("&gt;", ">")
                    .replace("&amp;", "&")
                    .replace("&quot;", "\"")
                    .replace("&apos;", "'");

            Log.d(TAG, "일반 태그 제거 결과: " + text.substring(0, Math.min(50, text.length())) + "...");
            return text;
        } catch (Exception e) {
            Log.e(TAG, "XML 파싱 오류", e);
            return "";
        }
    }

    /**
     * 테스트용 샘플 데이터 생성
     * @param context 컨텍스트
     */
    public static void createSampleFiles(Context context) {
        // 직무역량
        saveResponseFile(context, "직무역량",
                "<response>\n" +
                        "  <content>저는 JavaScript, React, Node.js 기반의 웹 개발 경험이 3년 있으며, RESTful API 설계와 데이터베이스 구축 경험이 있습니다. 특히 사용자 중심의 UI/UX 개발과 반응형 웹 디자인에 강점이 있으며, 현재는 TypeScript와 NextJS를 활용한 프로젝트를 진행하고 있습니다.</content>\n" +
                        "</response>");

        // 입사후포부
        saveResponseFile(context, "입사후포부",
                "<response>\n" +
                        "  <content>귀사에 입사하면 먼저 회사의 업무 프로세스와 문화에 빠르게 적응하여 팀에 조화롭게 융화되고자 합니다. 단기적으로는 프론트엔드 개발 역량을 발휘하여 사용자 경험을 향상시키는 데 기여하고, 장기적으로는 풀스택 개발자로 성장하여 팀과 회사의 성장에 더 큰 가치를 더하고 싶습니다.</content>\n" +
                        "</response>");

        // 지원동기
        saveResponseFile(context, "지원동기",
                "<response>\n" +
                        "  <content>귀사의 혁신적인 서비스와 사용자 중심의 철학, 그리고 개발자 친화적인 문화에 큰 매력을 느껴 지원하게 되었습니다. 특히 최근 출시한 모바일 서비스의 사용자 경험이 인상적이었으며, 이러한 서비스 개발에 참여하여 제 역량을 발휘하고 함께 성장하고 싶습니다.</content>\n" +
                        "</response>");

        // 성격장단점
        saveResponseFile(context, "성격장단점",
                "<response>\n" +
                        "  <content>저의 가장 큰 장점은 끈기와 책임감입니다. 맡은 일은 어떤 어려움이 있더라도 완성하려는 의지가 강하며, 팀원들과의 협업을 중시합니다. 다만 완벽주의적 성향이 있어 때로는 작은 문제에 너무 많은 시간을 투자하는 단점이 있습니다. 이를 개선하기 위해 시간 관리 기술을 지속적으로 연습하고 있습니다.</content>\n" +
                        "</response>");
    }

    public static String readSectionFromFile(Context context, String sectionKey) {
        String fileName = FILE_MAPPING.get(sectionKey);
        if (fileName == null) return "";

        try {
            File file = new File(context.getFilesDir(), fileName);
            if (!file.exists()) return "";

            FileInputStream fis = new FileInputStream(file);
            BufferedReader reader = new BufferedReader(new InputStreamReader(fis, StandardCharsets.UTF_8));
            StringBuilder builder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line).append("\n");
            }
            reader.close();
            return builder.toString().trim();
        } catch (Exception e) {
            Log.e("AIResponseFileManager", "파일 읽기 실패: " + fileName, e);
            return "";
        }
    }

}