package com.github.vmorev.crawler.utils.amazon;

import com.amazonaws.services.simpledb.AmazonSimpleDB;
import com.amazonaws.services.simpledb.AmazonSimpleDBClient;
import com.amazonaws.services.simpledb.model.*;
import com.github.vmorev.crawler.beans.SDBItem;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * User: Valentin_Morev
 * Date: 14.02.13
 */
public class SDBService extends AmazonService {
    private AmazonSimpleDB sdb;
    private Map<String, Domain> domains = new HashMap<>();

    public AmazonSimpleDB getSDB() {
        if (sdb == null)
            sdb = new AmazonSimpleDBClient(getCredentials());
        return sdb;
    }

    public <T extends SDBItem> Domain getDomain(String name, Class<T> clazz) {
        String domainName = name+"-"+clazz.getName();
        if (domains.get(domainName) == null) {
            domains.put(domainName, new Domain<>(name, clazz));
        }
        return domains.get(domainName);
    }

    public void listDomains(ListFunc<String> func) throws Exception {
        String nextToken = null;
        do {
            ListDomainsResult result = getSDB().listDomains((new ListDomainsRequest()).withNextToken(nextToken));
            nextToken = result.getNextToken() == null || result.getNextToken().equals("") ? null : result.getNextToken();
            for (String domainName : result.getDomainNames()) {
                func.process(domainName);
            }
        } while (nextToken != null);
    }

    public class Domain<T extends SDBItem> {
        private String name;
        private Class<T> clazz;

        public Domain(String name, Class<T> clazz) {
            this.name = name;
            this.clazz = clazz;
        }

        public String getName() {
            return name;
        }

        public void createDomain() throws Exception {
            if (!isDomainExists())
                getSDB().createDomain(new CreateDomainRequest(name));
        }

        public void deleteDomain() {
            getSDB().deleteDomain(new DeleteDomainRequest(name));
        }

        public void listObjects(String query, ListFunc<T> func) throws Exception {
            String nextToken = null;
            do {
                SelectResult result = getSDB().select(new SelectRequest(query).withNextToken(nextToken));
                nextToken = result.getNextToken() == null || result.getNextToken().equals("") ? null : result.getNextToken();
                for (Item item : result.getItems()) {
                    T obj = clazz.newInstance();
                    obj.fromSDB(item.getAttributes());
                    func.process(obj);
                }

            } while (nextToken != null);
        }

        public void saveObject(String itemName, T entity) throws IOException {
            PutAttributesRequest request = new PutAttributesRequest().withDomainName(name).withItemName(itemName).withAttributes(entity.toSDB());
            getSDB().putAttributes(request);
        }

        public T getObject(String itemName, boolean isReadConsistent) throws IOException, IllegalAccessException, InstantiationException {
            GetAttributesRequest request = (new GetAttributesRequest()).withDomainName(name).withItemName(itemName).withConsistentRead(isReadConsistent);
            T obj = clazz.newInstance();
            obj.fromSDB(getSDB().getAttributes(request).getAttributes());
            return obj;
        }

        //TODO implement usage
        protected boolean isItemExists(String domainName, String itemName) {
            GetAttributesRequest request = (new GetAttributesRequest()).withDomainName(domainName).withItemName(itemName).withConsistentRead(true);
            List<Attribute> attributes = getSDB().getAttributes(request).getAttributes();
            return attributes.size() > 0;
        }

        protected boolean isDomainExists() throws Exception {
            final boolean[] result = new boolean[1];
            result[0] = false;

            listDomains(new ListFunc<String>() {
                public void process(String domainName) {
                    if (domainName.equals(name))
                        result[0] = true;
                }
            });
            return result[0];
        }
    }
}
