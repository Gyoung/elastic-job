/*
 * Copyright 1999-2015 dangdang.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package com.dangdang.ddframe.reg.zookeeper;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.RetryOneTime;
import org.apache.zookeeper.KeeperException.NoAuthException;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public final class ZookeeperRegistryCenterForAuthTest {
    
    private static ZookeeperConfiguration zkConfig = new ZookeeperConfiguration(NestedTestingServer.getConnectionString(), ZookeeperRegistryCenterForAuthTest.class.getName());
    
    private static ZookeeperRegistryCenter zkRegCenter;
    
    @BeforeClass
    public static void setUp() {
        NestedTestingServer.start();
        zkConfig.setDigest("digest:password");
        zkConfig.setLocalPropertiesPath("conf/reg/local.properties");
        zkConfig.setSessionTimeoutMilliseconds(5000);
        zkConfig.setConnectionTimeoutMilliseconds(5000);
        zkRegCenter = new ZookeeperRegistryCenter(zkConfig);
    }
    
    @AfterClass
    public static void tearDown() {
        zkRegCenter.close();
    }
    
    @Test
    public void assertInitWithDigestSuccess() throws Exception {
        zkRegCenter.init();
        zkRegCenter.close();
        CuratorFramework client = CuratorFrameworkFactory.builder()
            .connectString(NestedTestingServer.getConnectionString())
            .retryPolicy(new RetryOneTime(2000))
            .authorization("digest", "digest:password".getBytes()).build();
        client.start();
        client.blockUntilConnected();
        assertThat(client.getData().forPath("/" + ZookeeperRegistryCenterForAuthTest.class.getName() + "/test/deep/nested"), is("deepNested".getBytes()));
    }
    
    @Test(expected = NoAuthException.class)
    public void assertInitWithDigestFailure() throws Exception {
        zkRegCenter.init();
        zkRegCenter.close();
        CuratorFramework client = CuratorFrameworkFactory.newClient(NestedTestingServer.getConnectionString(), new RetryOneTime(2000));
        client.start();
        client.blockUntilConnected();
        client.getData().forPath("/" + ZookeeperRegistryCenterForAuthTest.class.getName() + "/test/deep/nested");
    }
}
