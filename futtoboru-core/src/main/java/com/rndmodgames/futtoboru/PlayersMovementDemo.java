package com.rndmodgames.futtoboru;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.utils.CameraInputController;
import com.badlogic.gdx.utils.Array;

/**
 * Adds a polygonal (cube) player and allows movement with WASD + ARROWS
 * 
 * 	- Infinite scrolling background mesh
 *  - 1-2 Player movement and basic collision physics
 * 
 *  NOTE: we used a downloaded low poly ball texture (needs to have less than 32k vertices)
 *  
 *  	- we converted the .OBJ model to G3DB using the command line option tool:
 *  
 *  		> .\fbx-conv.exe .\football_ball.obj
 * 
 * @author Geomancer86
 */
public class PlayersMovementDemo extends ApplicationAdapter {

	public PerspectiveCamera cam;
    public CameraInputController camController;
    public ModelBatch modelBatch;
    public AssetManager assets;
    public Array<ModelInstance> instances = new Array<ModelInstance>();
    public Environment environment;
    public boolean loading;
	
	@Override
    public void create () {
		
		modelBatch = new ModelBatch();
        environment = new Environment();
        environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.4f, 0.4f, 0.4f, 1f));
        environment.add(new DirectionalLight().set(0.8f, 0.8f, 0.8f, -1f, -0.8f, -0.2f));

        cam = new PerspectiveCamera(67, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        
        cam.position.set(7f, 7f, 7f);
        
        cam.lookAt(0,0,0);
        cam.near = 1f;
        cam.far = 300f;
        cam.update();

        camController = new CameraInputController(cam);
        Gdx.input.setInputProcessor(camController);

        assets = new AssetManager();
        
        // load existing models
        assets.load("models/football_ball.g3db", Model.class);
        
        loading = true;
    }
	
	private void doneLoading() {
		
        Model ship = assets.get("models/football_ball.g3db", Model.class);

        for (float x = -5f; x <= 5f; x += 2f) {
            for (float z = -5f; z <= 5f; z += 2f) {
            	
                ModelInstance shipInstance = new ModelInstance(ship);
                shipInstance.transform.setToTranslation(x, 0, z);

                instances.add(shipInstance);
            }
        }
        
        loading = false;
    }

    @Override
    public void render () {
    	
        if (loading && assets.update()) {
        	doneLoading();
        }
        
        camController.update();

        Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

        modelBatch.begin(cam);
        modelBatch.render(instances, environment);
        modelBatch.end();
        
        /**
         * Test rotate ball models
         */
//        for (ModelInstance instance : instances) {
//        	
//        	instance.transform.rotate(MathUtils.random(4),
//        							  MathUtils.random(4),
//        							  MathUtils.random(4),
//        							  MathUtils.random(5));
//        }
    }

    @Override
    public void resize (int width, int height) {
    	
    }
    
    @Override
    public void dispose () {
        modelBatch.dispose();
        instances.clear();
        assets.dispose();
    }

    public void resume () {
    }

    public void pause () {
    }
}