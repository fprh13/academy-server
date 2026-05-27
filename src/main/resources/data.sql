-- 회원 더미 데이터
-- 수강생 비밀번호 : classmate1234@
-- 강사 비밀번호 : creator1234@
INSERT INTO users (id, login_id, password, email, name, role)
VALUES (1, 'classmate', '$2a$10$gfO5CByHR0yaDXFQphWYL.Vx.s4Zv6Du.KEPL3W6ivPrOtslCh8Li', 'classmate@test.com', '김수강', 'USER');

INSERT INTO users (id, login_id, password, email, name, role)
VALUES (2, 'creator', '$2a$10$EwqHV0GS4KF0cq4uKZXULOwVfleC2AHwMK3G5MxGgT1lqbiMKf4Kq', 'creator@test.com', '김강사', 'CREATOR');

-- 강의 더미 데이터

-- id : 1 / 수강생이 수강 신청한 강의 / 1번 강의 수강 신청 / 2번 강의 수강 확정 / 3번 강의 수강 취소 / 4번 강의 수강 확정이며, 최대 인원 1명으로 다른 수강생을 통해, 4번 강의로 웨이팅 테스트 가능
INSERT INTO courses (id, title, description, price, max_capacity, current_enrollment_count, start_date, end_date, state, creator_id)
VALUES (1, '자바 입문', '자바 문법과 객체지향 기본기를 익히는 입문 강의입니다.', 99000, 30, 1, DATE '2026-05-01', DATE '2026-06-30', 'OPEN', 2);
INSERT INTO courses (id, title, description, price, max_capacity, current_enrollment_count, start_date, end_date, state, creator_id)
VALUES (2, '스프링 부트 API 실전', '실무형 REST API 설계와 예외 처리, 검증 전략을 다룹니다.', 149000, 25, 1, DATE '2026-05-02', DATE '2026-07-10', 'OPEN', 2);
INSERT INTO courses (id, title, description, price, max_capacity, current_enrollment_count, start_date, end_date, state, creator_id)
VALUES (3, 'JPA 성능 최적화', '연관관계 매핑과 조회 성능 최적화 포인트를 집중적으로 다룹니다.', 169000, 20, 1, DATE '2026-05-03', DATE '2026-07-31', 'OPEN', 2);
INSERT INTO courses (id, title, description, price, max_capacity, current_enrollment_count, start_date, end_date, state, creator_id)
VALUES (4, '단 한명만 수강 가능합니다', '정원 1명 특강으로 웨이팅 동작을 확인하기 좋은 강의입니다.', 39000, 1, 0, DATE '2026-05-04', DATE '2026-06-16', 'OPEN', 2);

-- 초안 그리고 마감 강의
INSERT INTO courses (id, title, description, price, max_capacity, current_enrollment_count, start_date, end_date, state, creator_id)
VALUES (5, '도메인 주도 설계 초안', '아직 공개 전인 초안 강의입니다.', 129000, 15, 0, DATE '2026-05-05', DATE '2026-08-29', 'DRAFT', 2);
INSERT INTO courses (id, title, description, price, max_capacity, current_enrollment_count, start_date, end_date, state, creator_id)
VALUES (6, '테스트 코드 리팩터링', '모집 종료된 강의 예시 데이터입니다.', 89000, 20, 0, DATE '2026-05-06', DATE '2026-05-31', 'CLOSED', 2);

-- 예비 오픈 강의
INSERT INTO courses (id, title, description, price, max_capacity, current_enrollment_count, start_date, end_date, state, creator_id)
VALUES (7, '클린 아키텍처 실습', '계층 분리와 의존성 역전 원칙을 실습합니다.', 119000, 18, 0, DATE '2026-05-07', DATE '2026-08-02', 'OPEN', 2);
INSERT INTO courses (id, title, description, price, max_capacity, current_enrollment_count, start_date, end_date, state, creator_id)
VALUES (8, '쿠버네티스 기초', '배포와 운영 입문자를 위한 컨테이너 오케스트레이션 강의입니다.', 139000, 40, 0, DATE '2026-05-08', DATE '2026-09-07', 'OPEN', 2);
INSERT INTO courses (id, title, description, price, max_capacity, current_enrollment_count, start_date, end_date, state, creator_id)
VALUES (9, 'SQL 튜닝 워크숍', '인덱스와 실행 계획을 이해하는 실습형 워크숍입니다.', 79000, 12, 0, DATE '2026-05-09', DATE '2026-09-02', 'OPEN', 2);
INSERT INTO courses (id, title, description, price, max_capacity, current_enrollment_count, start_date, end_date, state, creator_id)
VALUES (10, '운영 장애 대응 회고', '실제 장애 사례를 기반으로 운영 대응 프로세스를 정리합니다.', 59000, 50, 0, DATE '2026-05-10', DATE '2026-09-15', 'OPEN', 2);

-- 수강 신청 더미 데이터
INSERT INTO enrollments (id, state, paid_at, cancelled_at, course_id, user_id)
VALUES (1, 'PENDING', NULL, NULL, 1, 1);
INSERT INTO enrollments (id, state, paid_at, cancelled_at, course_id, user_id)
VALUES (2, 'CONFIRMED', TIMESTAMP '2026-05-27 10:00:00', NULL, 2, 1);
INSERT INTO enrollments (id, state, paid_at, cancelled_at, course_id, user_id)
VALUES (3, 'CANCELLED', TIMESTAMP '2026-05-20 14:00:00', TIMESTAMP '2026-05-23 09:30:00', 3, 1);
INSERT INTO enrollments (id, state, paid_at, cancelled_at, course_id, user_id)
VALUES (4, 'CONFIRMED', TIMESTAMP '2026-05-26 16:00:00', NULL, 4, 1);

ALTER TABLE users ALTER COLUMN id RESTART WITH 100;
ALTER TABLE courses ALTER COLUMN id RESTART WITH 100;
ALTER TABLE enrollments ALTER COLUMN id RESTART WITH 100;
