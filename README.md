# Peeling-Project

내가 사용할 프로젝트의 껍데기 source

```java
- spring security (적용 완료)
- querydsl (Q클래스 생성 완료)
- jwt (적용 완료 ,refresh_token 까지 완료)
- oauth (google , naver 적용 완료)
- swagger (적용 완료)
- p6spy (적용 완료)
```

현재 security + jwt 개발을 완료 하였다.<br>

로그인 시 (header 또는 cookie) 를 통해 access_token / refresh_token /auto_login_chk_token 을 <br>
발급 하여 사용자에게 내려 준다.<br>

그 후 access_token이 만료 되었다면 refresh_token을 검증 하여 맞다면 <br>
access_token을 재 발급 하여 사용자에게 내려 준다. <br>

access_token 이 살아 있고 refresh_token의 만료일이 하루 남아 있다면 <br>
그 사용자의 refresh_token을 재 발급 하여 사용자에게 내려 준다. <br>

auto_login_chk_token이 false 일 경우는 access_token의 만료일을 체크 하여<br>
하루가 지났다면 로그인이 만료되었다고 판단을 하여 쿠키를 삭제 해준다.<br>
true일 경우는 로직을 타지 않는다.

refresh_token 이 없는 경우는 사용자 로그인이 되어 있지 않다고 판단을 하게 된다. <br>

로그아웃 시 cookie 일 경우 access_token 과 refresh_token 을 삭제 해준다.
header 일 경우는 client에서 넘어오기 때문에 상관 없을 거 같다.

최대한 생각을 하며 구글링을 하며 코드를 짰는데 아직 부족한 느낌이다.<br>
더 좋은 코드가 생각 나면 계속 수정을 해 나가야 겠다.

위에 정의된 라이브러리를 이용하여 프로젝트 틀을 만들어 두면<br> 
다음번에도 사용하기 편할거 같아서 미리 작업을 할 것이다.
