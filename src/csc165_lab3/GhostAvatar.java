package csc165_lab3;

import java.util.UUID;
import graphicslib3D.Vector3D;
import csc165_lab3.*;
import com.jogamp.common.nio.Buffers.*;
import java.nio.*;
import sage.scene.*;
import sage.event.*;
import graphicslib3D.*;

public class GhostAvatar extends TriMesh{
	public UUID id;
	public Vector3D pos;
	
	public GhostAvatar(UUID ghostID, Vector3D ghostPosition){
		id = ghostID;
		pos = ghostPosition;
	}
	
}