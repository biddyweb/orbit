/*
Copyright (C) 2015 Electronic Arts Inc.  All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions
are met:

1.  Redistributions of source code must retain the above copyright
    notice, this list of conditions and the following disclaimer.
2.  Redistributions in binary form must reproduce the above copyright
    notice, this list of conditions and the following disclaimer in the
    documentation and/or other materials provided with the distribution.
3.  Neither the name of Electronic Arts, Inc. ("EA") nor the names of
    its contributors may be used to endorse or promote products derived
    from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY ELECTRONIC ARTS AND ITS CONTRIBUTORS "AS IS" AND ANY
EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL ELECTRONIC ARTS OR ITS CONTRIBUTORS BE LIABLE FOR ANY
DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
(INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/

package com.ea.orbit.samples.helloworld;

import com.ea.orbit.actors.IActor;
import com.ea.orbit.actors.OrbitStage;
import com.ea.orbit.actors.test.ActorBaseTest;
import com.ea.orbit.actors.test.FakeClusterPeer;
import com.ea.orbit.actors.test.FakeStorageProvider;
import com.ea.orbit.samples.memoize.ExampleActor;
import com.ea.orbit.samples.memoize.IExample;
import com.ea.orbit.samples.memoize.MemoizeHookProvider;

import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class MemoizeTest extends ActorBaseTest
{
    @Test
    public void test()
    {
        OrbitStage stage1 = initStage();

        IExample memoize = IActor.getReference(IExample.class, "0");

        stage1.bind();

        long firstA = memoize.getNow("A").join();
        sleep(1000);
        long firstB = memoize.getNow("B").join();
        long secondA = memoize.getNow("A").join();
        assertTrue(ExampleActor.accessCount == 2);
        assertTrue(firstA != firstB);
        assertTrue(firstA == secondA);
        sleep(1000);
        long thirdA = memoize.getNow("A").join();
        long secondB = memoize.getNow("B").join();
        assertTrue(ExampleActor.accessCount == 2);
        assertTrue(thirdA != secondB);
        assertTrue(secondA == thirdA);
        assertTrue(firstB == secondB);
        sleep(4500);
        long fourthA = memoize.getNow("A").join();
        long thirdB = memoize.getNow("B").join();
        assertTrue(ExampleActor.accessCount == 4);
        assertTrue(thirdA != fourthA);
        assertTrue(secondB != thirdB);

    }

    public static void sleep(long millis)
    {
        try
        {
            Thread.sleep(millis);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public OrbitStage initStage()
    {
        OrbitStage stage = new OrbitStage();
        stage.setMode(OrbitStage.StageMode.HOST);
        stage.setExecutionPool(commonPool);
        stage.setMessagingPool(commonPool);
        stage.addProvider(new MemoizeHookProvider());
        stage.addProvider(new FakeStorageProvider(fakeDatabase));

        stage.setClock(clock);
        stage.setClusterName(clusterName);
        stage.setClusterPeer(new FakeClusterPeer());
        stage.start().join();
        stage.bind();
        return stage;
    }
}
