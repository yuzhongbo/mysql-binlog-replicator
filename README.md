# MySQL BinLog Replicator  [![Build Status](https://travis-ci.org/juanwolf/mysql-binlog-replicator.svg?branch=master)](https://travis-ci.org/juanwolf/mysql-binlog-replicator) [![Coverage Status](https://coveralls.io/repos/juanwolf/mysql-binlog-replicator/badge.svg?branch=master&service=github)](https://coveralls.io/github/juanwolf/mysql-binlog-replicator?branch=master)

This library will give you the ability to create a real time feeding from a mysql database to an ElasticSearch etc...
(all repository available in spring which extend the CRUDRepository interface in fact)

## Install ##

### Maven ###

    <dependency>
        <groupId>fr.juanwolf</groupId>
        <artifactId>mysql-binlog-replicator</artifactId>
        <version>1.0.7</version>
    </dependency>

### Setup the MYSQL server ###

First, configure your server to activate the replication.

To activate replication on the master, add this configuration to your mysql configuration file :

    [mysqld]
    server-id        = 1
    log_bin          = /var/log/mysql/mysql-bin.log
    expire_logs_days = 10
    max_binlog_size  = 100M
    binlog-format    = row # important to detect write and update event.


WARNING :
 
 As says in [mysql-binlog-connector-java](https://github.com/shyiko/mysql-binlog-connector-java#tapping-into-mysql-replication-stream), 
 Make sure that the user you're using has the REPLICATION SLAVE and REPLICATION CLIENT privileges.

### Setup the mysql-binlog-replicator ###

Create a mysql-binlog-replicator.properties with the mysql configuration :
    
    
    mysql.host=localhost
    mysql.port=3306
    # Tables that you want to track changes separated by commas
    mysql.schema=user
    # The package where you keep all your pojos
    mysql.scanmapping=fr.juanwolf.mysqlbinlogreplicator.domain
    # The user with replication access
    mysql.user=replicator
    # The password for the user with replication access
    mysql.password=Bernard Lama
    
### Setup modules configuration ###

#### ElasticSearch configuration ####

Make sure that you configured this variable to connect the service to your ElasticSearch :
    
    spring.data.elasticsearch.cluster-nodes=localhost:9300
    
    
## Create POJO's ##

### Use @MysqlMapping ###

Make sure you're using the @MysqlMapping annotation. It will bind the pojo to a specific table name and to your dao (repository).

For example a class for accounts would look like :
 
    @MysqlMapping(table = "user", repository="userRepository")
    public class Account {
        @Id
        String id;
    
        @Field(type = FieldType.Long, index = FieldIndex.analyzed)
        long identifier;
    
        @Field(type = FieldType.String, index = FieldIndex.analyzed)
        String mail;
    
        @Field(index = FieldIndex.analyzed, type = FieldType.Date)
        Date creationDate;
    
        @Field(index = FieldIndex.analyzed, type = FieldType.Float)
        float cartAmount;
    
        public Account() {}
    
        public float getCartAmount() {
            return cartAmount;
        }
    
        public Date getCreationDate() {
            return creationDate;
        }
    
        public void setMail(String mail) {
            this.mail = mail;
        }
    
        public long getIdentifier() {
            return identifier;
        }
    }

As you can see we keep the spring configuration for the elasticsearch repository AND the mysqlMapping annotation for the service.

### Type Mapping ###

You can convert different SQL types to java types. Only this ones are available :

| SQL types  | Java types       | Conditions                             | 
|------------|------------------|----------------------------------------|
| DATE       |  Date            | For yyyy-MM-dd SQL format              |
| DATETIME   |  Date            |                                        |
| DATETIME   |  String          | Need date.output format in config file |
| TIMESTAMP  |  sql.Timestamp   | For yyyy-MM-dd hh:mm:ss SQL format     |
| TIME       |  sql.Time        | For hh:MM:ss SQL format                |
| BIT        |  boolean         |                                        |
| TINY       |  boolean         |                                        |
| INT        |  int             |                                        |
| LONG       |  long            |                                        |
| FLOAT      |  float           |                                        |
| VARCHAR    |  String          |                                        |
| LONGTEXT   |  String          |                                        |
| TINYTEXT   |  String          |                                        |
    
If you need any other mapping, please open issue.

### Use the annotations for the repositories you'll use on your POJOs###

#### ElasticSearch ####

Add to your pojos annotations needed by the ElasticSearch Repository, example for the user class above :

    @Document(indexName = "user")
    @Mapping(mappingPath = "user")
    @MysqlMapping(table = "user", repository="userRepository")
    public class Account {
        ...
    }

### Create your main ###

- Autowire the mysqlBinLogService
- Create the main which call mysqlBinLogService.startReplication()
- It should work (if not please open an issue or/and contribute :D)
    
## How it works ##

Thanks to [shykio](https://github.com/shyiko) and his [mysql-binlog-connector-java](https://github.com/shyiko/mysql-binlog-connector-java), the mysql-binlog-replicator will detect every update of the binlog file from your mysqll server and then replicate it to your CRUD repository.
 
### Example ###

You can find an example of how to use it here : [mysql-binlog-replicator-example](http://github.com/juanwolf/mysql-binlog-replicator-example)

## Enhancement ##

The project does not support nested documents and does not apply indexes (could be a feature in next releases).
