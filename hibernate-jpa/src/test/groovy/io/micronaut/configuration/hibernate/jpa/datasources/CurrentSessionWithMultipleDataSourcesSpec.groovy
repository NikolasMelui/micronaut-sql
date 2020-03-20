/*
 * Copyright 2017-2020 original authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.micronaut.configuration.hibernate.jpa.datasources


import io.micronaut.configuration.hibernate.jpa.datasources.db1.ProductRepository
import io.micronaut.configuration.hibernate.jpa.datasources.db2.BookstoreMethodLevelTransaction
import io.micronaut.configuration.hibernate.jpa.datasources.db2.BookstoreRepository
import io.micronaut.context.ApplicationContext
import spock.lang.Specification

class CurrentSessionWithMultipleDataSourcesSpec extends Specification {

    void "test an application that defines 2 datasources uses correct transaction management"() {
        given:
        def context = ApplicationContext.run(
                'datasources.default.name': 'db1',
                'datasources.db2.url': 'jdbc:h2:mem:db2;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE',
                'jpa.default.entity-scan.packages': ['io.micronaut.configuration.hibernate.jpa.datasources.db1'],
                'jpa.default.properties.hibernate.hbm2ddl.auto':'create-drop',
                'jpa.db2.properties.hibernate.hbm2ddl.auto':'create-drop',
                'jpa.db2.entity-scan.packages': ['io.micronaut.configuration.hibernate.jpa.datasources.db2'],
        )

        ProductRepository productRepository = context.getBean(ProductRepository)
        BookstoreRepository bookstoreRepository = context.getBean(BookstoreRepository)
        BookstoreMethodLevelTransaction methodLevelTransaction = context.getBean(BookstoreMethodLevelTransaction)

        when:"Some data is saved in each database"
        def store1 = bookstoreRepository.save("Waterstones")
        def store2 = bookstoreRepository.save("Amazon")
        def product1 = productRepository.save("1234", "The Stand")
        def product2 = productRepository.save("1234", "The Stand")

        then:"The data is available"
        bookstoreRepository.findById(store1.id).isPresent()
        bookstoreRepository.findById(store2.id).isPresent()
        productRepository.findById(product1.id).isPresent()
        productRepository.findById(product2.id).isPresent()
        methodLevelTransaction.findById(store1.id).isPresent()
        methodLevelTransaction.findById(store2.id).isPresent()

        cleanup:
        context.close()

    }
}
