-- Erase schema if necessary
begin
    for rusr in (select username from dba_users where username like '%POC%')
        loop
            begin
                execute immediate 'drop user ' || rusr.username || ' cascade';
            exception
                when others then
                    raise;
            end;
        end loop;
end;
/

-- Setup script
begin
    ops$oracle.create_ts_role_sch_pool('POC', 'SCH_POC', 'POOL_POC');
end;
/

declare
    type v_array is varying array (20) of varchar2(50);
    schema v_array;
begin
    schema := v_array(
        'SCH_POC'
        );

    for i in 1..schema.count
        loop
            execute immediate 'grant connect to ' || schema(i);
            execute immediate 'grant create session to ' || schema(i);
            execute immediate 'grant create table to ' || schema(i);
            execute immediate 'grant create view to ' || schema(i);
            execute immediate 'grant create any trigger to ' || schema(i);
            execute immediate 'grant create any procedure to ' || schema(i);
            execute immediate 'grant create any sequence to ' || schema(i);
        end loop;
end;
/
