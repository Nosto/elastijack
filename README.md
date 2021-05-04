# Elastijack

Elastijack (aptly named) is a Jackson-based module for working with Elasticsearch documents. At the time of writing, Elastijac can only be used for mapping generation using Jackson, and also provides a mapper with the correct defaults to serde Elasticsearch documents. 

## Installation

Unfortunately, Elastijack is not available in any public Maven repositories except the GitHub Package Registry. For more information on how to install packages from the GitHub Package Registry, [https://docs.github.com/en/packages/guides/configuring-gradle-for-use-with-github-packages#installing-a-package][see the GitHub docs]

## Usage

As the Elastisearch clients provide no support for mapping generation, this module is supposed to be a drop-in module to augment existing functionality provided by the existing Datastax libraries.

Assume you have the following bean for which you would like to generate the mapping:

```java
import java.util.Date;

import com.datastax.oss.driver.api.mapper.annotations.CqlName;
import com.datastax.oss.driver.api.mapper.annotations.Entity;
import com.datastax.oss.driver.api.mapper.annotations.PartitionKey;
import com.nosto.elastijack.annotations.OrderedClusteringColumn;
import com.nosto.elastijack.types.FrozenList;

@Entity(defaultKeyspace = "mykeyspace")
@CqlName("brandsimilarities")
public class BrandSimilarities {

    @SuppressWarnings("DefaultAnnotationParam")
    @PartitionKey(0)
    @CqlName("brand")
    public String brand;

    @SuppressWarnings("DefaultAnnotationParam")
    @OrderedClusteringColumn(isAscending = false, value = 0)
    @CqlName("createdat")
    public Date createdAt;

    @OrderedClusteringColumn(isAscending = true, value = 1)
    @CqlName("skuid")
    public String skuId;

    @CqlName("related")
    public FrozenList<Relation> productRelations;

    @CqlName("relation")
    public static class Relation {

        @CqlName("brand")
        public String brand;

        @CqlName("skuid")
        public String skuId;

        @CqlName("score")
        public Float score;
    }
}
```

To generate the schema for the bean described above, you would run:

```scala
    val mapper = new CassandraJavaBeanMapper[BrandSimilarities]()
    val createSchema: String = mapper.generateMappingProperties
```

to yield the DDL:

```sql
CREATE 
  TYPE 
IF NOT 
EXISTS relation 
     ( score FLOAT
     , skuid TEXT
     , brand TEXT
     );

CREATE 
 TABLE 
IF NOT 
EXISTS brandsimilarities 
     ( brand TEXT
     , shardkey TINTINT
     , createdat TEXT
     , skuid TEXT
     , related LIST<FROZEN<relation>>
     , PRIMARY KEY
       ( ( brand
         , shardkey
          )
       , createdat
       , skuid
       )
    )
  WITH CLUSTERING 
 ORDER 
    BY 
     ( createdat DESC
     , skuid ASC
     );
```


## License

Apache License

Copyright (c) 2021 Nosto Oy.

[see the GitHub docs]: https://docs.github.com/en/packages/guides/configuring-gradle-for-use-with-github-packages#installing-a-package
