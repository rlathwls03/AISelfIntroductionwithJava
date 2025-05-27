package com.quicinc.chatapp;

// SelfIntroData 클래스 (자기소개서 내용 저장용)
class SelfIntroData {
    private String 직무역량;
    private String 입사후포부;
    private String 지원동기;
    private String 성격장단점;

    // 생성자, getter, setter...
    public SelfIntroData() {}

    public SelfIntroData(String 직무역량, String 입사후포부, String 지원동기, String 성격장단점) {
        this.직무역량 = 직무역량;
        this.입사후포부 = 입사후포부;
        this.지원동기 = 지원동기;
        this.성격장단점 = 성격장단점;
    }

    // getter와 setter 메서드들...
    public String get직무역량() { return 직무역량; }
    public void set직무역량(String 직무역량) { this.직무역량 = 직무역량; }

    public String get입사후포부() { return 입사후포부; }
    public void set입사후포부(String 입사후포부) { this.입사후포부 = 입사후포부; }

    public String get지원동기() { return 지원동기; }
    public void set지원동기(String 지원동기) { this.지원동기 = 지원동기; }

    public String get성격장단점() { return 성격장단점; }
    public void set성격장단점(String 성격장단점) { this.성격장단점 = 성격장단점; }
}
