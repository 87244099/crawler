ALTER DATABASE crawlernews CHARACTER SET = utf8mb4 COLLATE=utf8mb4_unicode_ci;

create table LINKS_TO_BE_PROCESS(
    link varchar(2000)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

create table LINKS_ALREADY_PROCESSED(
    link varchar(2000)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

create table NEWS(
    id bigint primary key auto_increment,
    title text,
    content text,
    url varchar(2000),
    created_at timestamp default now(),
    modified_at timestamp default now()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;