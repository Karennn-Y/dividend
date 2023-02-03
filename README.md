# DIVIDENED PROJECT (배당금 조회 프로젝트)
## Table of contents
* [ABOUT THE PROJECT](#about-the-project)
* [BUILD WITH](#build-with)
* [API](#api)
## ABOUT THE PROJECT
- 미국 주식 배당금 정보를 제공하는 API 서비스를 개발합니다.
  - 웹 페이지를 분석하고 스크래핑 기법을 활용하여 필요한 데이터를 추출/저장합니다. (https://finance.yahoo.com/ 사용)
  - 사용자별 데이터를 관리하고 예상 배당금 액수를 계산할 수 있습니다.
  - 서비스에서 캐시의 필요성을 이해하고 캐시 서버를 구성합니다.
## BUILD WITH
- SpringBoot v 2.5.6
- redis
- H2
- JPA
- jsoup
- JWT
- Spring Security
## API
### /company

- Post : "/company"
  - ticker를 입력받아 회사 정보를 추가
  - 입력받은 ticker로 회사 정보를 찾을 수 없으면 error 반환(400, "존재하지 않는 회사명 입니다.")
  - 이미 추가되어있는 회사를 중복 추가시 error 반환(400, "이미 존재하는 회사입니다.")

- Get : "/company"
  - 저장되어 있는 모든 회사 목록 반환
  - 조회시 'READ' role 이 있는 유저만 조회 가능

- Get : "/company/autoComplete"
  - 자동완성기능
  - keyword(prefix)를 입력받은 후 keyword로 시작하는 회사명 10개 반환

- Delete : "/company/{ticker}"
  - ticker 에 해당하는 회사 정보 삭제 
  - 'WRITE' role 이 있는 유저만 삭제 가능

### /finance

- Get : "/finance/dividend/{companyName}
  - 회사명으로 해당 회사 배당금 정보 조회
  - 잘못된 회사명일 경우 error 반환(400, "존재하지 않는 회사명 입니다.")

### /auth

- Post : "/auth/signup"
  - "username", "password", "roles" (-> List type) 를 입력받아 회원가입
  - 입력받은 "password"는 암호화 처리
  - 이미 가입된 "username" 입력시 error 반환(400, "이미 존재하는 유저입니다.")

- Post : "/auth/signin"
  - "username", "password" 를 입력받아 로그인 처리
  - 로그인 성공시 jwt 토큰 발급
  - 잘못된 "username" 입력시 error 반환(400, "사용자 정보가 존재하지 않습니다.")
  - 잘못된 "password" 입력시 error 반환(400, "비밀번호가 일치하지 않습니다.")




