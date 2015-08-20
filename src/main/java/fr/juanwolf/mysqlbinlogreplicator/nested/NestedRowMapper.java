package fr.juanwolf.mysqlbinlogreplicator.nested;

import fr.juanwolf.mysqlbinlogreplicator.DomainClass;
import fr.juanwolf.mysqlbinlogreplicator.annotations.MysqlMapping;
import fr.juanwolf.mysqlbinlogreplicator.annotations.NestedMapping;
import fr.juanwolf.mysqlbinlogreplicator.component.DomainClassAnalyzer;
import fr.juanwolf.mysqlbinlogreplicator.nested.requester.SQLRequester;
import fr.juanwolf.mysqlbinlogreplicator.service.MySQLEventListener;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.RowMapper;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Date;

/**
 * Created by juanwolf on 11/08/15.
 */
@Slf4j
public class NestedRowMapper implements RowMapper {

    Class nestedObjectClass;

    Field[] fields;

    DomainClassAnalyzer domainClassAnalyzer;

    public NestedRowMapper(Class nestedObjectClass, DomainClassAnalyzer domainClassAnalyzer) {
        this.nestedObjectClass = nestedObjectClass;
        fields = this.nestedObjectClass.getDeclaredFields();
        this.domainClassAnalyzer = domainClassAnalyzer;
    }

    @Override
    public Object mapRow(ResultSet resultSet, int i) throws SQLException {
        try {
            Constructor nestedConstructor = nestedObjectClass.getConstructor();
            Object nestedObject = nestedConstructor.newInstance();
            for (Field field : fields) {
                field.setAccessible(true);
                if (field.getType() == int.class || field.getType() == Integer.class) {
                    int intValue = resultSet.getInt(field.getName());
                    field.set(nestedObject, intValue);
                } else if (field.getType() == String.class) {
                    String resultString = resultSet.getString(field.getName());
                    field.set(nestedObject, resultString);
                } else if (field.getType() == java.sql.Date.class || field.getType() == java.util.Date.class) {
                    Date dateValue = resultSet.getDate(field.getName());
                    if (field.getType() == java.sql.Date.class) {
                        field.set(nestedObject, dateValue);
                    } else {
                        if (dateValue != null) {
                            java.util.Date date = new Date(dateValue.getTime());
                            field.set(nestedObject, date);
                        } else {
                            field.set(nestedObject, null);
                        }
                    }
                } else if (field.getType() == long.class) {
                    long longValue = resultSet.getLong(field.getName());
                    field.set(nestedObject, longValue);
                } else if (field.getType() == boolean.class) {
                    boolean booleanValue = resultSet.getBoolean(field.getName());
                    field.set(nestedObject, booleanValue);
                } else if (field.getType() == Time.class) {
                    Time timeValue = resultSet.getTime(field.getName());
                    field.set(nestedObject, timeValue);
                } else if (field.getType() == float.class) {
                    float floatValue = resultSet.getFloat(field.getName());
                    field.set(nestedObject, floatValue);
                } else if (field.getType() == double.class) {
                    double doubleValue = resultSet.getDouble(field.getName());
                    field.set(nestedObject, doubleValue);
                } else if (field.getType() == java.sql.Timestamp.class) {
                    Timestamp timestampValue = resultSet.getTimestamp(field.getName());
                    field.set(nestedObject, timestampValue);
                } else {
                    MysqlMapping mysqlMapping = (MysqlMapping) nestedObjectClass.getAnnotation(MysqlMapping.class);
                    DomainClass domainClass = domainClassAnalyzer.getDomainClassMap().get(mysqlMapping.table());
                    SQLRequester sqlRequester = domainClass.getSqlRequesters().get(field.getName());
                    NestedMapping nestedMapping = field.getAnnotation(NestedMapping.class);
                    if (nestedMapping != null) {
                        Object nestedTmpObject = sqlRequester.queryForeignEntity(sqlRequester.getForeignKey(),
                                sqlRequester.getPrimaryKeyForeignEntity(),
                                resultSet.getString(nestedMapping.foreignKey()));
                        field.set(nestedObject, nestedTmpObject);
                    }
                }
                field.setAccessible(false);
            }
            return nestedObject;
        } catch (NoSuchMethodException e) {
            log.error("No empty constructor for the class {}. Please, create one to make this object instantiable.",
                    nestedObjectClass, e);
        } catch (Exception exception) {
            log.error("An exception occurred: ", exception);
        }
        return null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        NestedRowMapper that = (NestedRowMapper) o;

        return this.nestedObjectClass == that.nestedObjectClass;
    }

    @Override
    public int hashCode() {
        return nestedObjectClass.hashCode() * fields.hashCode() + 4;
    }
}
