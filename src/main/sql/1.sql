create table if not exists votes (
    label varchar(32),
    count int
);

insert into Votes values ('cats', 0);
insert into Votes values ('dogs', 0);
