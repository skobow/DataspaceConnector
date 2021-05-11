/*
 * Copyright 2020 Fraunhofer Institute for Software and Systems Engineering
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
//package io.dataspaceconnector.model.view;
//
//import io.dataspaceconnector.model.Catalog;
//import io.dataspaceconnector.model.CatalogDesc;
//import io.dataspaceconnector.model.CatalogFactory;
//import org.junit.Assert;
//import org.junit.Before;
//import org.junit.Test;
//
//public class CatalogViewerTest {
//
//    private CatalogViewFactory factory;
//
//    @Before
//    public void init() {
//        factory = new CatalogViewFactory();
//    }
//
//    @Test(expected = NullPointerException.class)
//    public void create_null_throwNullPointerException() {
//        /* ARRANGE */
//        // Nothing to arrange.
//
//        /* ACT && ASSERT*/
//        factory.create(null);
//    }
//
//    @Test
//    public void create_validDesc_validView() {
//        final var catalog = getCatalog();
//
//        final var view = factory.create(catalog);
//
//        Assert.assertNotNull(view);
//        Assert.assertEquals(view.getTitle(), catalog.getTitle());
//        Assert.assertEquals(view.getDescription(), catalog.getDescription());
//    }
//
//
//    Catalog getCatalog() {
//        final var catalogFactory = new CatalogFactory();
//
//        final var desc = new CatalogDesc();
//        desc.setTitle("Some Title");
//        desc.setDescription("Value");
//
//        return catalogFactory.create(desc);
//    }
//}