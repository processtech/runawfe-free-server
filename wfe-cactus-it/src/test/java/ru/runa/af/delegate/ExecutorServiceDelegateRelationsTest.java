package ru.runa.af.delegate;

import java.util.List;

import junit.framework.Assert;

import org.apache.cactus.ServletTestCase;

import ru.runa.af.service.ServiceTestHelper;
import ru.runa.wfe.presentation.BatchPresentationFactory;
import ru.runa.wfe.relation.Relation;
import ru.runa.wfe.relation.RelationPair;
import ru.runa.wfe.service.RelationService;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.user.Actor;

public class ExecutorServiceDelegateRelationsTest extends ServletTestCase {
    private ServiceTestHelper th;

    private RelationService relationService;

    @Override
    protected void setUp() throws Exception {
        relationService = Delegates.getRelationService();
        th = new ServiceTestHelper(ExecutorServiceDelegateRelationsTest.class.getName());
        th.createDefaultExecutorsMap();
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        th.releaseResources();
        super.tearDown();
    }

    /**
     * Test for adding and removing relation groups. No relation pairs added; no relation pairs testing. Loading relations with
     * {@link ru.runa.wfe.presentation.BatchPresentations} test.
     */
    public void testAddRemoveRelationGroup() throws Exception {
        String groupName = "Relation1";
        String groupName2 = "Relation2";
        Relation relationGroup = relationService.createRelation(th.getAdminUser(), new Relation(groupName, groupName));
        Assert.assertEquals(groupName, relationGroup.getName());
        List<Relation> groups = relationService.getRelations(th.getAdminUser(), BatchPresentationFactory.RELATIONS.createDefault());
        Assert.assertEquals(1, groups.size());
        Assert.assertEquals(groups.get(0).getName(), groupName);
        Relation relationGroup2 = relationService.createRelation(th.getAdminUser(), new Relation(groupName2, groupName2));
        groups = relationService.getRelations(th.getAdminUser(), BatchPresentationFactory.RELATIONS.createDefault());
        Assert.assertEquals(2, groups.size());
        Assert.assertTrue((groups.get(0).getName().equals(groupName) && groups.get(1).getName().equals(groupName2))
                || (groups.get(0).getName().equals(groupName2) && groups.get(1).getName().equals(groupName)));
        relationService.removeRelation(th.getAdminUser(), relationGroup.getId());
        groups = relationService.getRelations(th.getAdminUser(), BatchPresentationFactory.RELATIONS.createDefault());
        Assert.assertEquals(1, groups.size());
        Assert.assertEquals(groups.get(0).getName(), groupName2);
        relationService.removeRelation(th.getAdminUser(), relationGroup2.getId());
        groups = relationService.getRelations(th.getAdminUser(), BatchPresentationFactory.RELATIONS.createDefault());
        Assert.assertEquals(0, groups.size());
    }

    /**
     * Add/remove relation pairs test. Simple test for relation pair loading.
     */
    public void testAddRemoveRelation() throws Exception {
        String groupName = "Relation1";
        String groupName2 = "Relation2";
        Relation relationGroup = relationService.createRelation(th.getAdminUser(), new Relation(groupName, groupName));
        Relation relationGroup2 = relationService.createRelation(th.getAdminUser(), new Relation(groupName2, groupName2));
        Actor a1 = th.createActorIfNotExist("1", "1");
        Actor a2 = th.createActorIfNotExist("2", "2");
        Actor a3 = th.createActorIfNotExist("3", "3");
        relationService.addRelationPair(th.getAdminUser(), relationGroup.getId(), a1, a3);
        relationService.addRelationPair(th.getAdminUser(), relationGroup2.getId(), a2, a3);
        relationService.addRelationPair(th.getAdminUser(), relationGroup.getId(), a1, a3);
        relationService.addRelationPair(th.getAdminUser(), relationGroup2.getId(), a1, a3);
        List<RelationPair> relations = relationService.getRelationPairs(th.getAdminUser(), groupName,
                BatchPresentationFactory.RELATION_PAIRS.createDefault());
        assertEquals(1, relations.size());
        assertEquals(a1, relations.get(0).getLeft());
        assertEquals(a3, relations.get(0).getRight());
        RelationPair toRemove = relations.get(0);
        relations = relationService.getRelationPairs(th.getAdminUser(), groupName2, BatchPresentationFactory.RELATION_PAIRS.createDefault());
        assertEquals(2, relations.size());
        assertTrue(((relations.get(0).getLeft().equals(a2) && relations.get(0).getRight().equals(a3))
                && (relations.get(1).getLeft().equals(a1) && relations.get(1).getRight().equals(a3)))
                || ((relations.get(1).getLeft().equals(a2) && relations.get(1).getRight().equals(a3))
                        && (relations.get(0).getLeft().equals(a1) && relations.get(0).getRight().equals(a3))));
        relationService.removeRelationPair(th.getAdminUser(), toRemove.getId());
        assertEquals(0,
                relationService.getRelationPairs(th.getAdminUser(), groupName, BatchPresentationFactory.RELATION_PAIRS.createDefault()).size());
        relationService.removeRelation(th.getAdminUser(), relationGroup.getId());
        relationService.removeRelation(th.getAdminUser(), relationGroup2.getId());
    }
}
