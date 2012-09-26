/*
 * Copyright 2012 Alex Usachev, thothbot@gmail.com
 * 
 * This file is part of Parallax project.
 * 
 * Parallax is free software: you can redistribute it and/or modify it 
 * under the terms of the GNU General Public License as published by the 
 * Free Software Foundation, either version 3 of the License, or (at your 
 * option) any later version.
 * 
 * Parallax is distributed in the hope that it will be useful, but 
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY 
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License 
 * for more details.
 * 
 * You should have received a copy of the GNU General Public License along with 
 * Parallax. If not, see http://www.gnu.org/licenses/.
 */

package thothbot.parallax.demo.client.content.plugins;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import thothbot.parallax.core.client.context.Canvas3d;
import thothbot.parallax.core.client.controls.TrackballControls;
import thothbot.parallax.core.client.gl2.enums.PixelFormat;
import thothbot.parallax.core.client.gl2.enums.TextureMagFilter;
import thothbot.parallax.core.client.gl2.enums.TextureMinFilter;
import thothbot.parallax.core.client.gl2.enums.TextureWrapMode;
import thothbot.parallax.core.client.shaders.Shader;
import thothbot.parallax.core.client.shaders.Uniform;
import thothbot.parallax.core.client.textures.RenderTargetTexture;
import thothbot.parallax.core.client.textures.Texture;
import thothbot.parallax.core.shared.Log;
import thothbot.parallax.core.shared.cameras.OrthographicCamera;
import thothbot.parallax.core.shared.cameras.PerspectiveCamera;
import thothbot.parallax.core.shared.core.Color;
import thothbot.parallax.core.shared.core.Geometry;
import thothbot.parallax.core.shared.core.Mathematics;
import thothbot.parallax.core.shared.core.Vector2;
import thothbot.parallax.core.shared.geometries.PlaneGeometry;
import thothbot.parallax.core.shared.lights.AmbientLight;
import thothbot.parallax.core.shared.lights.DirectionalLight;
import thothbot.parallax.core.shared.lights.PointLight;
import thothbot.parallax.core.shared.materials.Material;
import thothbot.parallax.core.shared.materials.MeshBasicMaterial;
import thothbot.parallax.core.shared.materials.MeshLambertMaterial;
import thothbot.parallax.core.shared.materials.ShaderMaterial;
import thothbot.parallax.core.shared.objects.Mesh;
import thothbot.parallax.core.shared.scenes.FogSimple;
import thothbot.parallax.core.shared.scenes.Scene;
import thothbot.parallax.core.shared.utils.UniformsUtils;
import thothbot.parallax.demo.client.ContentWidget;
import thothbot.parallax.demo.client.Demo;
import thothbot.parallax.demo.client.DemoAnnotations.DemoSource;
import thothbot.parallax.demo.resources.TerrainShader;
import thothbot.parallax.loader.shared.JsonLoader;
import thothbot.parallax.plugin.postprocessing.client.BloomPass;
import thothbot.parallax.plugin.postprocessing.client.Postprocessing;
import thothbot.parallax.plugin.postprocessing.client.RenderPass;
import thothbot.parallax.plugin.postprocessing.client.ShaderPass;
import thothbot.parallax.plugin.postprocessing.client.shaders.BleachbypassShader;
import thothbot.parallax.plugin.postprocessing.client.shaders.HorizontalTiltShiftShader;
import thothbot.parallax.plugin.postprocessing.client.shaders.LuminosityShader;
import thothbot.parallax.plugin.postprocessing.client.shaders.NormalMapShader;
import thothbot.parallax.plugin.postprocessing.client.shaders.VerticalTiltShiftShader;

import com.google.gwt.core.client.Duration;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.RunAsyncCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.resources.client.TextResource;
import com.google.gwt.user.client.rpc.AsyncCallback;

public final class TerrainDynamic extends ContentWidget 
{
	
	/*
	 * Load shaders
	 */
	@DemoSource
	public interface Resources extends Shader.DefaultResources
	{
		Resources INSTANCE = GWT.create(Resources.class);

		@Source("../../../resources/shaders/terrain_dynamic_noise.fs")
		TextResource getFragmentShader();
		
		@Source("../../../resources/shaders/terrain_dynamic_noise.vs")
		TextResource getVertexShader();
	}

	/*
	 * Prepare Rendering Scene
	 */
	@DemoSource
	class DemoScene extends DemoAnimatedScene implements Texture.ImageLoadHandler
	{
		private static final String diffuseImage1 = "./static/textures/terrain/grasslight-big.jpg";
		private static final String diffuseImage2 = "./static/textures/terrain/backgrounddetailed6.jpg";
		private static final String detailImage = "./static/textures/terrain/grasslight-big-nm.jpg";
		
		private static final String parrotModel = "./static/models/animated/parrot.js";
		private static final String flamingoModel = "./static/models/animated/flamingo.js";
		private static final String storkModel = "./static/models/animated/stork.js";
		
		private static final boolean updateNoise = true;
		private static final boolean animateTerrain = false;
		
		OrthographicCamera cameraOrtho;
		Scene sceneRenderTarget;
		
		Map<String, ShaderMaterial> mlib;
		List<Mesh> morphs;
		
		Map<String, Uniform> uniformsTerrain;
		Map<String, Uniform> uniformsNoise;
		
		RenderTargetTexture heightMap;
		RenderTargetTexture normalMap;
		
		DirectionalLight directionalLight;
		PointLight pointLight;
		
		TrackballControls controls;
		Postprocessing composer;
		
		Mesh terrain;
		Mesh quadTarget;
		
		int textureCounter;
		
		double animDelta = 0;
		int animDeltaDir = -1;
		double lightVal = 0;
		int lightDir = 1;
		
		int SCREEN_WIDTH, SCREEN_HEIGHT;
		
		private double oldTime;
		
		@Override
		protected void loadCamera()
		{
			setCamera(
					new PerspectiveCamera(
							40, // fov
							getRenderer().getCanvas().getAspectRation(), // aspect 
							2, // near
							4000 // far 
					)); 
			
			Canvas3d canvas = getRenderer().getCanvas();
			
			cameraOrtho = new OrthographicCamera( 
					canvas.getWidth() / - 2.0, 
					canvas.getWidth() / 2.0, 
					canvas.getHeight() / 2.0, 
					canvas.getHeight() / - 2.0, 
					-10000, 10000);
		}
		
		@Override
		protected void onResize() 
		{
			super.onResize();
			Canvas3d canvas = getRenderer().getCanvas();

			SCREEN_WIDTH = canvas.getWidth();
			SCREEN_HEIGHT = canvas.getHeight();
		}

		@Override
		protected void onStart()
		{
			getCamera().getPosition().set( -1200, 800, 1200 );
			cameraOrtho.getPosition().setZ( 100 );
			getScene().add(getCamera());

			onResize();
//			soundtrack = document.getElementById( "soundtrack" );

			// SCENE (RENDER TARGET)

			sceneRenderTarget = new Scene();
			sceneRenderTarget.add( cameraOrtho );

			// CAMERA

			controls = new TrackballControls( getCamera(), getRenderer().getCanvas() );
			controls.getTarget().set( 0 );

			controls.setRotateSpeed(1.0);
			controls.setZoomSpeed(1.2);
			controls.setPanSpeed(0.8);

			controls.setZoom(true);
			controls.setPan(true);

			controls.setStaticMoving(true);
			controls.setDynamicDampingFactor(0.15);

			// SCENE (FINAL)

			getScene().setFog( new FogSimple( 0x050505, 2000, 4000 ) );
			getScene().getFog().getColor().setHSV( 0.102, 0.9, 0.825 );

			// LIGHTS

			getScene().add( new AmbientLight( 0x111111 ) );

			directionalLight = new DirectionalLight( 0xffffff, 1.15 );
			directionalLight.getPosition().set( 500, 2000, 0 );
			getScene().add( directionalLight );

			pointLight = new PointLight( 0xff4400, 1.5, 0 );
			pointLight.getPosition().set( 0 );
			getScene().add( pointLight );

			// HEIGHT + NORMAL MAPS

			int rx = 256, ry = 256;

			heightMap  = new RenderTargetTexture( rx, ry );
			heightMap.setMinFilter(TextureMinFilter.LINEAR_MIPMAP_LINEAR);
			heightMap.setMagFilter(TextureMagFilter.LINEAR);
			heightMap.setFormat(PixelFormat.RGB);
			
			normalMap = new RenderTargetTexture( rx, ry );
			normalMap.setMinFilter(TextureMinFilter.LINEAR_MIPMAP_LINEAR);
			normalMap.setMagFilter(TextureMagFilter.LINEAR);
			normalMap.setFormat(PixelFormat.RGB);

			NormalMapShader normalShader = new NormalMapShader();

			Map<String, Uniform> uniformsNormal = UniformsUtils.clone( normalShader.getUniforms() );

			uniformsNormal.get("height").setValue( 0.05 );
			((Vector2)uniformsNormal.get("resolution").getValue()).set( rx, ry );
			uniformsNormal.get("heightMap").setValue( heightMap );

			// TEXTURES

			final RenderTargetTexture specularMap = new RenderTargetTexture( 2048, 2048 );
			specularMap.setMinFilter(TextureMinFilter.LINEAR_MIPMAP_LINEAR);
			specularMap.setMagFilter(TextureMagFilter.LINEAR);
			specularMap.setFormat(PixelFormat.RGB);
			specularMap.setWrapS(TextureWrapMode.REPEAT);
			specularMap.setWrapT(TextureWrapMode.REPEAT);

			Texture diffuseTexture1 = new Texture( diffuseImage1, new Texture.ImageLoadHandler() {
				
				@Override
				public void onImageLoad(Texture texture) {
					DemoScene.this.onImageLoad(texture);
					DemoScene.this.applyShader( new LuminosityShader(), texture, specularMap );
				}
			});

			diffuseTexture1.setWrapS(TextureWrapMode.REPEAT);
			diffuseTexture1.setWrapT(TextureWrapMode.REPEAT);

			Texture diffuseTexture2 = new Texture( diffuseImage2, this);

			diffuseTexture2.setWrapS(TextureWrapMode.REPEAT);
			diffuseTexture2.setWrapT(TextureWrapMode.REPEAT);

			Texture detailTexture = new Texture( detailImage, this );
			detailTexture.setWrapS(TextureWrapMode.REPEAT);
			detailTexture.setWrapT(TextureWrapMode.REPEAT);

			// TERRAIN SHADER

			TerrainShader terrainShader = new TerrainShader();

			uniformsTerrain = UniformsUtils.clone( terrainShader.getUniforms() );

			uniformsTerrain.get( "tNormal" ).setValue( normalMap );
			uniformsTerrain.get( "uNormalScale" ).setValue( 3.5 );

			uniformsTerrain.get( "tDisplacement" ).setValue( heightMap );

			uniformsTerrain.get( "tDiffuse1" ).setValue( diffuseTexture1 );
			uniformsTerrain.get( "tDiffuse2" ).setValue( diffuseTexture2 );
			uniformsTerrain.get( "tSpecular" ).setValue( specularMap );
			uniformsTerrain.get( "tDetail" ).setValue( detailTexture );

			uniformsTerrain.get( "enableDiffuse1" ).setValue( 1 );
			uniformsTerrain.get( "enableDiffuse2" ).setValue( 1 );
			uniformsTerrain.get( "enableSpecular" ).setValue( 1 );

			((Color)uniformsTerrain.get( "uDiffuseColor" ).getValue()).setHex( 0xffffff );
			((Color)uniformsTerrain.get( "uSpecularColor").getValue()).setHex( 0xffffff );
			((Color)uniformsTerrain.get( "uAmbientColor" ).getValue()).setHex( 0x111111 );

			uniformsTerrain.get( "uShininess" ).setValue( 30.0 );
			uniformsTerrain.get( "uDisplacementScale" ).setValue( 375 );

			((Vector2)uniformsTerrain.get( "uRepeatOverlay" ).getValue()).set( 6, 6 );

			uniformsNoise = new HashMap<String, Uniform>();
			uniformsNoise.put("time", new Uniform(Uniform.TYPE.F, 1.0));
			uniformsNoise.put("scale", new Uniform(Uniform.TYPE.V2, new Vector2( 1.5, 1.5 )));
			uniformsNoise.put("offset", new Uniform(Uniform.TYPE.V2, new Vector2( 0, 0 )));
			
			mlib = new HashMap<String, ShaderMaterial>();
			
			ShaderMaterial materialHeightmap = new ShaderMaterial(Resources.INSTANCE);
			materialHeightmap.getShader().setUniforms(uniformsNoise);
			materialHeightmap.setLights(false);
			materialHeightmap.setFog(true);
			mlib.put("heightmap", materialHeightmap);
			
			ShaderMaterial materialNormal = new ShaderMaterial(normalShader);
			materialNormal.getShader().setUniforms(uniformsNormal);
			materialNormal.setLights(false);
			materialNormal.setFog(true);
			mlib.put("normal", materialNormal);
			
			ShaderMaterial materialTerrain = new ShaderMaterial(terrainShader);
			materialTerrain.getShader().setUniforms(uniformsTerrain);
			materialTerrain.setLights(true);
			materialTerrain.setFog(true);
			mlib.put("terrain", materialTerrain);

			PlaneGeometry plane = new PlaneGeometry( SCREEN_WIDTH, SCREEN_HEIGHT );
			MeshBasicMaterial planeMaterial = new MeshBasicMaterial();
			planeMaterial.setColor(new Color(0x000000));
			quadTarget = new Mesh( plane, planeMaterial );
			quadTarget.getPosition().setZ( -500 );
			sceneRenderTarget.add( quadTarget );

			// TERRAIN MESH

			PlaneGeometry geometryTerrain = new PlaneGeometry( 6000, 6000, 256, 256 );

			geometryTerrain.computeFaceNormals();
			geometryTerrain.computeVertexNormals();
			geometryTerrain.computeTangents();

			terrain = new Mesh( geometryTerrain, materialTerrain );
			terrain.getPosition().set( 0, -125, 0 );
			terrain.getRotation().setX( -Math.PI / 2 );
			terrain.setVisible(false);
			getScene().add( terrain );

			// RENDERER

			getRenderer().setClearColor(getScene().getFog().getColor(), 1.0);
			getRenderer().setGammaInput(true);
			getRenderer().setGammaOutput(true);

			// EVENTS

//			document.addEventListener( 'keydown', onKeyDown, false );

			// COMPOSER

			getRenderer().setAutoClear(false);
			
			RenderPass renderModel = new RenderPass( getScene(), getCamera() );
			
			BloomPass effectBloom = new BloomPass( 0.6 );
			ShaderPass effectBleach = new ShaderPass( new BleachbypassShader() );
			effectBleach.getUniforms().get( "opacity" ).setValue( 0.65 );

			ShaderPass hblur = new ShaderPass( new HorizontalTiltShiftShader() );
			ShaderPass vblur = new ShaderPass( new VerticalTiltShiftShader() );

			int bluriness = 6;

			hblur.getUniforms().get( "h" ).setValue( bluriness / SCREEN_WIDTH );
			vblur.getUniforms().get( "v" ).setValue( bluriness / SCREEN_HEIGHT );
			hblur.getUniforms().get( "r" ).setValue( 0.5 ); 
			vblur.getUniforms().get( "r" ).setValue( 0.5 );
			vblur.setRenderToScreen(true);

			RenderTargetTexture renderTarget = new RenderTargetTexture( SCREEN_WIDTH, SCREEN_HEIGHT );
			specularMap.setMinFilter(TextureMinFilter.LINEAR);
			specularMap.setMagFilter(TextureMagFilter.LINEAR);
			specularMap.setFormat(PixelFormat.RGB);
			specularMap.setStencilBuffer(false);
			
			composer = new Postprocessing( getRenderer(), getScene(), renderTarget );
			composer.addPass( renderModel );
			composer.addPass( effectBloom );
			composer.addPass( effectBleach );

			composer.addPass( hblur );
			composer.addPass( vblur );
			
			this.oldTime = Duration.currentTimeMillis();
		}
		
		private void addMorph( Geometry geometry, double speed, double duration, double x, double y, double z ) 
		{
			MeshLambertMaterial material = new MeshLambertMaterial();
			material.setColor(new Color(0xffaa55));
			material.setMorphTargets(true);
			material.setVertexColors(Material.COLORS.FACE);

//			MorphAnimation meshAnim = new MorphAnimation( geometry, material );
//
//			meshAnim.speed = speed;
//			meshAnim.setDuration(duration);
//			meshAnim.time = 600 * Math.random();
//
//			meshAnim.position.set( x, y, z );
//			meshAnim.rotation.y = Math.PI/2;
//
//			meshAnim.castShadow = true;
//			meshAnim.receiveShadow = false;
//
//			getScene().add( meshAnim );
//
//			morphs.add( meshAnim );

			getRenderer().initWebGLObjects( getScene() );

			final JsonLoader jsonLoader = new JsonLoader();

			final double startX = -3000;
			try
			{
				jsonLoader.load(parrotModel, new JsonLoader.ModelLoadHandler() {
					
					@Override
					public void onModeLoad() {
						Geometry geometry = jsonLoader.getGeometry();

						morphColorsToFaceColors( geometry );
						addMorph( geometry, 250, 500, startX -500, 500, 700 );
						addMorph( geometry, 250, 500, startX - Math.random() * 500, 500, -200 );
						addMorph( geometry, 250, 500, startX - Math.random() * 500, 500, 200 );
						addMorph( geometry, 250, 500, startX - Math.random() * 500, 500, 1000 );
						
					}
				});
				
				jsonLoader.load(flamingoModel, new JsonLoader.ModelLoadHandler() {
					
					@Override
					public void onModeLoad() {
						Geometry geometry = jsonLoader.getGeometry();

						morphColorsToFaceColors( geometry );
						addMorph( geometry, 500, 1000, startX - Math.random() * 500, 350, 40 );
					}
				});
				
				jsonLoader.load(storkModel, new JsonLoader.ModelLoadHandler() {

					@Override
					public void onModeLoad() {
						Geometry geometry = jsonLoader.getGeometry();

						morphColorsToFaceColors( geometry );
						addMorph( geometry, 350, 1000, startX - Math.random() * 500, 350, 340 );
					}
				});

			}
			catch (RequestException exception) 
			{
				Log.error("Error while loading JSON file.");
			}
			
			// PRE-INIT

			getRenderer().initWebGLObjects( getScene() );
		}

		private void morphColorsToFaceColors( Geometry geometry ) 
		{
			if ( geometry.getMorphColors() != null && geometry.getMorphColors().size() > 0 ) 
			{
				Geometry.MorphColor colorMap = geometry.getMorphColors().get(0);

				for ( int i = 0; i < colorMap.colors.size(); i ++ ) 
				{
					geometry.getFaces().get(i).setColor( colorMap.colors.get(i) );
				}
			}
		}
		
		private void applyShader( Shader shader, Texture texture, RenderTargetTexture target ) 
		{
			ShaderMaterial shaderMaterial = new ShaderMaterial(shader);

			shaderMaterial.getShader().getUniforms().get("tDiffuse").setValue(texture);

			Scene sceneTmp = new Scene();

			Mesh meshTmp = new Mesh( new PlaneGeometry( SCREEN_WIDTH, SCREEN_HEIGHT ), shaderMaterial );
			meshTmp.getPosition().setZ( -500 );

			sceneTmp.add( meshTmp );

			getRenderer().render( sceneTmp, cameraOrtho, target, true );
		}
		
		@Override
		public void onImageLoad(Texture texture)
		{
			textureCounter += 1;

			if ( textureCounter == 3 )	
			{
				terrain.setVisible(true);

//				document.getElementById( "loading" ).style.display = "none";
			}
		}
		
		@Override
		protected void onStop()
		{			
		}
		
		@Override
		protected void onUpdate(double duration)
		{
			double delta = (Duration.currentTimeMillis() - this.oldTime) * 0.001;

			//			soundVal = Mathematics.clamp( soundVal + delta * soundDir, 0, 1 );
			//
			//			if ( soundVal !== oldSoundVal ) 
			//			{
			//				if ( soundtrack ) 
			//				{
			//					soundtrack.volume = soundVal;
			//					oldSoundVal = soundVal;
			//				}
			//			}

			if ( terrain.isVisible() ) 
			{
				controls.update();

				double time = duration * 0.001;

				double fLow = 0.4, fHigh = 0.825;

				lightVal = Mathematics.clamp( lightVal + 0.5 * delta * lightDir, fLow, fHigh );
				double valNorm = ( lightVal - fLow ) / ( fHigh - fLow );

				double sat = Mathematics.mapLinear( valNorm, 0, 1, 0.95, 0.25 );
				getScene().getFog().getColor().setHSV( 0.1, sat, lightVal );

				getRenderer().setClearColor( getScene().getFog().getColor(), 1 );

				directionalLight.setIntensity( Mathematics.mapLinear( valNorm, 0, 1, 0.1, 1.15 ) );
				pointLight.setIntensity( Mathematics.mapLinear( valNorm, 0, 1, 0.9, 1.5 ) );

				uniformsTerrain.get( "uNormalScale" ).setValue( Mathematics.mapLinear( valNorm, 0, 1, 0.6, 3.5 ) );

				if ( updateNoise ) 
				{
					animDelta = Mathematics.clamp( animDelta + 0.00075 * animDeltaDir, 0, 0.05 );
					uniformsNoise.get( "time" ).setValue( (Double)uniformsNoise.get( "time" ).getValue() + delta * animDelta );
					((Vector2)uniformsNoise.get( "offset" ).getValue()).addX( delta * 0.05 );

					((Vector2)uniformsTerrain.get( "uOffset" ).getValue()).setX( 4 * ((Vector2)uniformsNoise.get( "offset" ).getValue()).getX() );

					quadTarget.setMaterial( mlib.get( "heightmap" ));
					getRenderer().render( sceneRenderTarget, cameraOrtho, heightMap, true );

					quadTarget.setMaterial( mlib.get( "normal" ));
					getRenderer().render( sceneRenderTarget, cameraOrtho, normalMap, true );

					//updateNoise = false;
				}

//				for ( int i = 0; i < morphs.size(); i ++ ) 
//				{
//					Mesh morph = morphs.get( i );
//
//					morph.updateAnimation( 1000 * delta );
//
//					morph.getPosition().addX( morph.speed * delta );
//
//					if ( morph.getPosition().getX()  > 2000 )  
//					{
//						morph.getPosition().setX( -1500 - Math.random() * 500 );
//					}
//				}
			}
			
			this.oldTime = Duration.currentTimeMillis();
		}
	}
		
	public TerrainDynamic() 
	{
		super("Dynamic procedural terrain", "Used 3d simplex noise. Options - day / night: [n]; animate terrain: [m]. This example based on the three.js example.");
	}
	
	@Override
	public DemoScene onInitialize()
	{
		return new DemoScene();
	}

	@Override
	public ImageResource getIcon()
	{
		return Demo.resources.exampleTerrainDynamic();
	}
	
	@Override
	protected void asyncOnInitialize(final AsyncCallback<DemoAnimatedScene> callback)
	{
		GWT.runAsync(TerrainDynamic.class, new RunAsyncCallback() 
		{
			public void onFailure(Throwable caught)
			{
				callback.onFailure(caught);
			}

			public void onSuccess()
			{
				callback.onSuccess(onInitialize());
			}
		});
	}
}
