/*
 * Copyright 2012 Alex Usachev, thothbot@gmail.com
 * 
 * This file is part of Parallax project.
 * 
 * Parallax is free software: you can redistribute it and/or modify it 
 * under the terms of the Creative Commons Attribution 3.0 Unported License.
 * 
 * Parallax is distributed in the hope that it will be useful, but 
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY 
 * or FITNESS FOR A PARTICULAR PURPOSE. See the Creative Commons Attribution 
 * 3.0 Unported License. for more details.
 * 
 * You should have received a copy of the the Creative Commons Attribution 
 * 3.0 Unported License along with Parallax. 
 * If not, see http://creativecommons.org/licenses/by/3.0/.
 */

package thothbot.parallax.demo.resources;

import thothbot.parallax.core.client.shaders.Shader;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.TextResource;

/**
 * Beckmann distribution function
 * <p>
 * - to be used in specular term of skin shader<br>
 * - render a screen-aligned quad to precompute a 512 x 512 texture
 * - from <a href="http://developer.nvidia.com/node/171">nvidia.com</a>
 * <p>
 * Based on three.js.code
 * 
 * @author thothbot
 *
 */
public final class BeckmannShader extends Shader 
{
	interface Resources extends DefaultResources
	{
		Resources INSTANCE = GWT.create(Resources.class);
		
		@Source("shaders/beckmann.vs")
		TextResource getVertexShader();

		@Source("shaders/beckmann.fs")
		TextResource getFragmentShader();
	}

	public BeckmannShader() 
	{
		super(Resources.INSTANCE);
	}

	@Override
	protected void initUniforms() {

	}
}
