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
    private ServiceTestHelper h;
    private RelationService relationService;

    @Override
    protected void setUp() {
        relationService = Delegates.getRelationService();
        h = new ServiceTestHelper(getClass().getName());
        h.createDefaultExecutorsMap();
    }

    @Override
    protected void tearDown() {
        h.releaseResources();
    }

    /**
     * Test for adding and removing relation groups. No relation pairs added; no relation pairs testing. Loading relations with
     * {@link ru.runa.wfe.presentation.BatchPresentation} test.
     */
    public void testAddRemoveRelationGroup() {
        String groupName = "Relation1";
        String groupName2 = "Relation2";
        Relation relationGroup = relationService.createRelation(h.getAdminUser(), new Relation(groupName, groupName));
        Assert.assertEquals(groupName, relationGroup.getName());
        List<Relation> groups = relationService.getRelations(h.getAdminUser(), BatchPresentationFactory.RELATIONS.createDefault());
        Assert.assertEquals(1, groups.size());
        Assert.assertEquals(groups.get(0).getName(), groupName);
        Relation relationGroup2 = relationService.createRelation(h.getAdminUser(), new Relation(groupName2, groupName2));
        groups = relationService.getRelations(h.getAdminUser(), BatchPresentationFactory.RELATIONS.createDefault());
        Assert.assertEquals(2, groups.size());
        Assert.assertTrue((groups.get(0).getName().equals(groupName) && groups.get(1).getName().equals(groupName2))
                || (groups.get(0).getName().equals(groupName2) && groups.get(1).getName().equals(groupName)));
        relationService.removeRelation(h.getAdminUser(), relationGroup.getId());
        groups = relationService.getRelations(h.getAdminUser(), BatchPresentationFactory.RELATIONS.createDefault());
        Assert.assertEquals(1, groups.size());
        Assert.assertEquals(groups.get(0).getName(), groupName2);
        relationService.removeRelation(h.getAdminUser(), relationGroup2.getId());
        groups = relationService.getRelations(h.getAdminUser(), BatchPresentationFactory.RELATIONS.createDefault());
        Assert.assertEquals(0, groups.size());
    }

    /**
     * Add/remove relation pairs test. Simple test for relation pair loading.
     */
    public void testAddRemoveRelation() {
        String groupName = "Relation1";
        String groupName2 = "Relation2";
        Relation relationGroup = relationService.createRelation(h.getAdminUser(), new Relation(groupName, groupName));
        Relation relationGroup2 = relationService.createRelation(h.getAdminUser(), new Relation(groupName2, groupName2));
        Actor a1 = h.createActorIfNotExist("1", "1");
        Actor a2 = h.createActorIfNotExist("2", "2");
        Actor a3 = h.createActorIfNotExist("3", "3");
        relationService.addRelationPair(h.getAdminUser(), relationGroup.getId(), a1, a3);
        relationService.addRelationPair(h.getAdminUser(), relationGroup2.getId(), a2, a3);
        relationService.addRelationPair(h.getAdminUser(), relationGroup.getId(), a1, a3);
        relationService.addRelationPair(h.getAdminUser(), relationGroup2.getId(), a1, a3);
        List<RelationPair> relations = relationService.getRelationPairs(h.getAdminUser(), groupName,
                BatchPresentationFactory.RELATION_PAIRS.createDefault());
        assertEquals(1, relations.size());
        assertEquals(a1, relations.get(0).getLeft());
        assertEquals(a3, relations.get(0).getRight());
        RelationPair toRemove = relations.get(0);
        relations = relationService.getRelationPairs(h.getAdminUser(), groupName2, BatchPresentationFactory.RELATION_PAIRS.createDefault());
        assertEquals(2, relations.size());
        assertTrue(((relations.get(0).getLeft().equals(a2) && relations.get(0).getRight().equals(a3))
                && (relations.get(1).getLeft().equals(a1) && relations.get(1).getRight().equals(a3)))
                || ((relations.get(1).getLeft().equals(a2) && relations.get(1).getRight().equals(a3))
                        && (relations.get(0).getLeft().equals(a1) && relations.get(0).getRight().equals(a3))));
        relationService.removeRelationPair(h.getAdminUser(), toRemove.getId());
        assertEquals(0,
                relationService.getRelationPairs(h.getAdminUser(), groupName, BatchPresentationFactory.RELATION_PAIRS.createDefault()).size());
        relationService.removeRelation(h.getAdminUser(), relationGroup.getId());
        relationService.removeRelation(h.getAdminUser(), relationGroup2.getId());
    }
}
