package com.example.aiselfintroduction;

import android.content.Context;
import android.util.Log;
import java.io.File;

/**
 * XML 파일 존재 여부를 체크하고 테스트 XML 파일을 생성하는 유틸리티 클래스
 */
public class XMLFileChecker {
    private static final String TAG = "XMLFileChecker";

    /**
     * 모든 AI 응답 XML 파일 존재 여부 체크
     * @param context 컨텍스트
     * @return 상태 메시지
     */
    public static String checkXMLFiles(Context context) {
        StringBuilder status = new StringBuilder();

        // 각 파일의 존재 여부 체크
        checkFile(context, AIResponseFileManager.FILE_ROLE, status);
        checkFile(context, AIResponseFileManager.FILE_GOAL, status);
        checkFile(context, AIResponseFileManager.FILE_REASON, status);
        checkFile(context, AIResponseFileManager.FILE_PERSONALITY, status);

        return status.toString();
    }

    /**
     * 특정 파일의 존재 여부 체크
     * @param context 컨텍스트
     * @param fileName 파일명
     * @param status 상태 메시지를 저장할 StringBuilder
     */
    private static void checkFile(Context context, String fileName, StringBuilder status) {
        File file = new File(context.getFilesDir(), fileName);
        status.append(fileName).append(": ").append(file.exists() ? "존재함" : "없음").append("\n");
    }

    /**
     * 테스트용 XML 파일 생성 (테스트 환경에서만 사용)
     * @param context 컨텍스트
     */
    public static void createTestXMLFiles(Context context) {
        // 임시 테스트 XML 파일 작성
        String roleXml =
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                        "<response>\n" +
                        "    <content>\n" +
                        "        <![CDATA[\n" +
                        "        저는 Android 앱 개발 경력이 3년이며, Java와 Kotlin을 사용한 다양한 프로젝트를 진행했습니다. MVVM 아키텍처와 Clean Architecture에 대한 이해가 깊고, RESTful API 통합, Room 데이터베이스 관리, Retrofit을 이용한 네트워크 통신에 능숙합니다. 또한 GitHub Actions를 활용한 CI/CD 경험도 있습니다.\n" +
                        "        ]]>\n" +
                        "    </content>\n" +
                        "</response>";

        String goalXml =
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                        "<response>\n" +
                        "    <content>\n" +
                        "        <![CDATA[\n" +
                        "        귀사에 입사하면 먼저 개발 프로세스와 코드베이스를 빠르게 습득하여 팀에 조화롭게 합류하고자 합니다. 단기적으로는 모바일 앱 성능 최적화와 사용자 경험 향상에 기여하고, 장기적으로는 Android 아키텍트로 성장하여 앱 아키텍처 설계와 기술적 의사결정에 참여하고 싶습니다.\n" +
                        "        ]]>\n" +
                        "    </content>\n" +
                        "</response>";

        String reasonXml =
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                        "<response>\n" +
                        "    <content>\n" +
                        "        <![CDATA[\n" +
                        "        귀사의 모바일 앱이 사용자 중심적 디자인과 안정적인 성능으로 업계에서 높은 평가를 받고 있는 것을 알고 지원하게 되었습니다. 특히 지속적인 기술 혁신과 개발자 친화적인 문화가 저의 커리어 목표와 일치하여 함께 성장하고 싶습니다.\n" +
                        "        ]]>\n" +
                        "    </content>\n" +
                        "</response>";

        String personalityXml =
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                        "<response>\n" +
                        "    <content>\n" +
                        "        <![CDATA[\n" +
                        "        저의 가장 큰 장점은 문제 해결에 대한 끈기와 분석적 사고력입니다. 복잡한 버그나 성능 이슈를 해결하는 것을 즐기며, 체계적으로 접근하는 편입니다. 다만 완벽주의적 성향으로 때로는 작은 문제에 지나치게 시간을 투자하는 경향이 있어, 최근에는 시간 관리와 우선순위 설정에 중점을 두고 개선하고 있습니다.\n" +
                        "        ]]>\n" +
                        "    </content>\n" +
                        "</response>";

        // 파일 저장
        try {
            context.openFileOutput(AIResponseFileManager.FILE_ROLE, Context.MODE_PRIVATE)
                    .write(roleXml.getBytes());

            context.openFileOutput(AIResponseFileManager.FILE_GOAL, Context.MODE_PRIVATE)
                    .write(goalXml.getBytes());

            context.openFileOutput(AIResponseFileManager.FILE_REASON, Context.MODE_PRIVATE)
                    .write(reasonXml.getBytes());

            context.openFileOutput(AIResponseFileManager.FILE_PERSONALITY, Context.MODE_PRIVATE)
                    .write(personalityXml.getBytes());

            Log.d(TAG, "테스트 XML 파일 생성 완료");
        } catch (Exception e) {
            Log.e(TAG, "테스트 XML 파일 생성 실패", e);
        }
    }

    /**
     * XML 파일 내용 출력 (디버그용)
     * @param context 컨텍스트
     */
    public static void printXMLFileContents(Context context) {
        Log.d(TAG, "==== XML 파일 내용 ====");

        try {
            // 직무역량
            if (new File(context.getFilesDir(), AIResponseFileManager.FILE_ROLE).exists()) {
                String content = AIResponseFileManager.readResponseFile(context, "직무역량");
                Log.d(TAG, "직무역량 XML:\n" + content);
            }

            // 입사후포부
            if (new File(context.getFilesDir(), AIResponseFileManager.FILE_GOAL).exists()) {
                String content = AIResponseFileManager.readResponseFile(context, "입사후포부");
                Log.d(TAG, "입사후포부 XML:\n" + content);
            }

            // 지원동기
            if (new File(context.getFilesDir(), AIResponseFileManager.FILE_REASON).exists()) {
                String content = AIResponseFileManager.readResponseFile(context, "지원동기");
                Log.d(TAG, "지원동기 XML:\n" + content);
            }

            // 성격장단점
            if (new File(context.getFilesDir(), AIResponseFileManager.FILE_PERSONALITY).exists()) {
                String content = AIResponseFileManager.readResponseFile(context, "성격장단점");
                Log.d(TAG, "성격장단점 XML:\n" + content);
            }
        } catch (Exception e) {
            Log.e(TAG, "XML 파일 내용 출력 실패", e);
        }
    }
}
