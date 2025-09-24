CREATE DATABASE IF NOT EXISTS 'security';

CREATE USER 'security'@'%' IDENTIFIED BY '1234';

GRANT ALL PRIVILEGES ON security.* TO 'security'@'%';
FLUSH PRIVILEGES;

create table TEST(
                     test_id int primary key auto_increment
    , anything varchar(20) not null
    , age int not null default 20
);
insert into test(anything) values('아무거나');


create table USER(
                     user_id int primary key auto_increment
    , id varchar(20) not null
    , password varchar(64) NOT NULL
    , age int not null default 20
);

-- 1. 기존의 'id' 컬럼을 'username'으로 이름 및 속성 변경
ALTER TABLE user
    CHANGE COLUMN id username VARCHAR(255) NOT NULL UNIQUE;

-- 2. 'role' 컬럼 추가
ALTER TABLE user
    ADD COLUMN role VARCHAR(255) NOT NULL;

CREATE TABLE ROLE(
                     role_id int primary key auto_increment
    , role_name varchar(20) NOT NULL
);

insert into ROLE(role_name)
values('USER'), ('STORE'), ('ADMIN');
