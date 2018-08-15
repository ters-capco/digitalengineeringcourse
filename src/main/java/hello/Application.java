package hello;

import com.fasterxml.classmate.TypeResolver;
import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.async.DeferredResult;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.builders.ResponseMessageBuilder;
import springfox.documentation.schema.ModelRef;
import springfox.documentation.schema.WildcardType;
import springfox.documentation.service.ApiKey;
import springfox.documentation.service.AuthorizationScope;
import springfox.documentation.service.SecurityReference;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.contexts.SecurityContext;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import javax.annotation.PostConstruct;
import java.time.LocalDate;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static com.mongodb.client.model.Filters.eq;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static springfox.documentation.schema.AlternateTypeRules.newRule;

@SpringBootApplication
@EnableSwagger2
@RestController
@Configuration
@PropertySource("classpath:db.properties")
public class Application {
    private static final Logger LOGGER = LoggerFactory.getLogger(Application.class);

    @Value("${database.mongo.host}")
    private String DB_HOST;

    @Value("${database.mongo.port}")
    private int DB_PORT;

    @Value("${database.mongo.name}")
    private String DB_NAME;

    @Bean
    public MongoDatabase db() {
        return mongoClient().getDatabase(DB_NAME);
    }

    @Bean
    public MongoClient mongoClient() {
        return new MongoClient(DB_HOST, DB_PORT);
    }

    @RequestMapping("/helloWorld")
    public String home() {
        return "Hello World";
    }

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @PostConstruct
    public void initialize(){
        LOGGER.info("Connecting to mongo db");
    }

    @RequestMapping("/users/list")
    public String usersList(){
        MongoCollection<Document> collection = db().getCollection("test");
        FindIterable<Document> iterDoc = collection.find();

        StringBuilder sb = new StringBuilder();

        for (Document anIterDoc : iterDoc) {
            sb.append(anIterDoc.toJson());
        }
        LOGGER.info("Getting user list");

        return sb.toString();
    }

    @RequestMapping(value = "/users/get/{id}", method = GET)
    public ResponseEntity usersGet(@PathVariable("id") String id){
        MongoCollection<Document> collection = db().getCollection("test");
        Document doc = collection.find(eq("_id", new ObjectId(id))).first();

        if (doc == null) {
            LOGGER.error("request user not found");

            return new ResponseEntity(HttpStatus.NOT_FOUND);
        }
        else {
            return new ResponseEntity<>(doc.toJson(), HttpStatus.OK);
        }
    }


    @PostMapping("/insert")
    public ResponseEntity insert(@RequestBody String data){
        JSONObject jsonObj = new JSONObject(data);

        MongoCollection<Document> collection = db().getCollection("test");

        Document document = new Document()
                .append("firstName", jsonObj.get("firstName"))
                .append("lastName", jsonObj.get("lastName"));
        collection.insertOne(document);

        ObjectId id = (ObjectId)document.get( "_id" );

        return new ResponseEntity<>(id.toString(), HttpStatus.CREATED);
    }


    @Bean
    public Docket petApi() {
        return new Docket(DocumentationType.SWAGGER_2).select().apis(RequestHandlerSelectors.any()).paths(PathSelectors.any()).build().pathMapping("/")
                .directModelSubstitute(LocalDate.class, String.class).genericModelSubstitutes(ResponseEntity.class)
                .alternateTypeRules(newRule(typeResolver.resolve(DeferredResult.class, typeResolver.resolve(ResponseEntity.class, WildcardType.class)), typeResolver.resolve(WildcardType.class)))
                .useDefaultResponseMessages(false)
                .globalResponseMessage(GET, newArrayList(new ResponseMessageBuilder().code(500).message("500 message").responseModel(new ModelRef("Error")).build()))
                .securitySchemes(newArrayList(apiKey())).securityContexts(newArrayList(securityContext()));
    }

    @Autowired
    private TypeResolver typeResolver;

    private ApiKey apiKey() {
        return new ApiKey("mykey", "api_key", "header");
    }

    private SecurityContext securityContext() {
        return SecurityContext.builder().securityReferences(defaultAuth()).forPaths(PathSelectors.regex("/anyPath.*")).build();
    }

    List<SecurityReference> defaultAuth() {
        AuthorizationScope authorizationScope = new AuthorizationScope("global", "accessEverything");
        AuthorizationScope[] authorizationScopes = new AuthorizationScope[1];
        authorizationScopes[0] = authorizationScope;
        return newArrayList(new SecurityReference("mykey", authorizationScopes));
    }

}