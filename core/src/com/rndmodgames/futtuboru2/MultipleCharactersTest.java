package com.rndmodgames.futtuboru2;

import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.collision.btAxisSweep3;
import com.badlogic.gdx.physics.bullet.collision.btCollisionDispatcher;
import com.badlogic.gdx.physics.bullet.collision.btConvexShape;
import com.badlogic.gdx.physics.bullet.collision.btDefaultCollisionConfiguration;
import com.badlogic.gdx.physics.bullet.collision.btGhostPairCallback;
import com.badlogic.gdx.physics.bullet.collision.btPairCachingGhostObject;
import com.badlogic.gdx.physics.bullet.dynamics.btDiscreteDynamicsWorld;
import com.badlogic.gdx.physics.bullet.dynamics.btKinematicCharacterController;
import com.badlogic.gdx.physics.bullet.dynamics.btSequentialImpulseConstraintSolver;
import com.rndmodgames.futtoboru.BaseBulletTest;
import com.rndmodgames.futtoboru.BulletEntity;
import com.rndmodgames.futtoboru.BulletWorld;

public class MultipleCharactersTest extends BaseBulletTest {

	BulletEntity ground;
	
	btGhostPairCallback ghostPairCallback;
	btPairCachingGhostObject ghostObject;
	btConvexShape ghostShape;
	btKinematicCharacterController characterController;
	Matrix4 characterTransform;
	Vector3 characterDirection = new Vector3();
	Vector3 walkDirection = new Vector3();
	
	@Override
	public BulletWorld createWorld () {
		
		// We create the world using an axis sweep broadphase for this test
		btDefaultCollisionConfiguration collisionConfiguration = new btDefaultCollisionConfiguration();
		btCollisionDispatcher dispatcher = new btCollisionDispatcher(collisionConfiguration);
		btAxisSweep3 sweep = new btAxisSweep3(new Vector3(-1000, -1000, -1000), new Vector3(1000, 1000, 1000));
		btSequentialImpulseConstraintSolver solver = new btSequentialImpulseConstraintSolver();
		btDiscreteDynamicsWorld collisionWorld = new btDiscreteDynamicsWorld(dispatcher, sweep, solver, collisionConfiguration);
		ghostPairCallback = new btGhostPairCallback();
		sweep.getOverlappingPairCache().setInternalGhostPairCallback(ghostPairCallback);
		
		return new BulletWorld(collisionConfiguration, dispatcher, sweep, solver, collisionWorld);
	}
	
	@Override
	public void create () {
		
		super.create();
		
		/**
		 * Add the Football Field
		 * 
		 * 	- Standard Measures:
		 */
		
		
		
		// Add the ground
//		(ground = world.add("ground", 0f, 0f, 0f))
//					   .setColor(0.25f + 0.5f * (float) Math.random(), 0.25f + 0.5f * (float) Math.random(), 0.25f + 0.5f * (float) Math.random(), 1f);
	}
	
	
	
	
	
	@Override
	protected void renderWorld () {
		// TODO Auto-generated method stub
		super.renderWorld();
	}
		
   	@Override
   	public boolean touchDown(float x, float y, int pointer, int button) {

	   	return false;
   	}
	
	@Override
	public boolean tap (float x, float y, int count, int button) {

		shoot(x, y);
		return true;
	}
	
	@Override
	public void dispose () {
//		((btDiscreteDynamicsWorld)(world.collisionWorld)).removeAction(characterController);
//		world.collisionWorld.removeCollisionObject(ghostObject);
//		super.dispose();
//		characterController.dispose();
//		ghostObject.dispose();
//		ghostShape.dispose();
//		ghostPairCallback.dispose();
//		ground = null;
	}
}