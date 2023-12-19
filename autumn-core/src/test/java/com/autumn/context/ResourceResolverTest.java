package com.autumn.context;


import org.junit.Test;

import java.util.List;

public class ResourceResolverTest {


    @Test
    public void scan() {
        ResourceResolver rr = new ResourceResolver("com.autumn");
        List<Resource> scan = rr.scan(res -> res);
        for (Resource resource : scan) {
            System.out.println(resource.path());
        }

    }
}
