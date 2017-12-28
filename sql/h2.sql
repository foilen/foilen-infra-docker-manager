
    create table failed_container_state (
        container_name varchar(255) not null,
        container_run_unique_id varchar(100),
        container_started_unique_id varchar(100),
        image_unique_id varchar(100),
        last_state varchar(255),
        primary key (container_name)
    );

    create table installed_unix_user (
        username varchar(255) not null,
        primary key (username)
    );

    create table redirect_endpoint (
        machine_container_endpoint varchar(255) not null,
        ip varchar(100),
        port integer not null,
        primary key (machine_container_endpoint)
    );

    create table running_container_state (
        container_name varchar(255) not null,
        container_run_unique_id varchar(100),
        container_started_unique_id varchar(100),
        image_unique_id varchar(100),
        ip varchar(100),
        primary key (container_name)
    );

    create index redirect_endpoint_port on redirect_endpoint (port);
