package mindustry.world.blocks.distribution;

import arc.graphics.g2d.*;
import arc.math.*;
import arc.util.*;
import mindustry.annotations.Annotations.*;
import mindustry.entities.units.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.world.blocks.payloads.*;
import mindustry.world.blocks.*;
import mindustry.content.*;

public class PayloadRouter extends PayloadConveyor{
    public @Load("@-over") TextureRegion overRegion;

    public PayloadRouter(String name){
        super(name);

        outputsPayload = true;
        outputFacing = false;
    }

    @Override
    public void drawRequestRegion(BuildPlan req, Eachable<BuildPlan> list){
        super.drawRequestRegion(req, list);

        Draw.rect(overRegion, req.drawx(), req.drawy());
    }

    public class PayloadRouterBuild extends PayloadConveyorBuild implements ControlBlock{
        public float smoothRot;

        public @Nullable BlockUnitc unit;

        @Override
        public Unit unit(){
            if(unit == null){
                unit = (BlockUnitc)UnitTypes.block.create(team);
                unit.tile(this);
            }
            return (Unit)unit;
        }

        @Override
        public boolean canControl(){
            return size == 1 || true;
        }

        @Override
        public boolean shouldAutoTarget(){
            return false;
        }
        
        @Override
        public void add(){
            super.add();
            smoothRot = rotdeg();
        }

        public void pickNext(){
            if(item != null){
                int rotations = 0;
                do{
                    rotation = (rotation + 1) % 4;
                    onProximityUpdate();
                    //this condition intentionally uses "accept from itself" conditions, because payload conveyors only accept during the start
                    //"accept from self" conditions are for dropped payloads and are less restrictive
                }while((blocked || next == null || !next.acceptPayload(next, item)) && ++rotations < 4);
            }
        }

        @Override
        public void handlePayload(Building source, Payload payload){
            super.handlePayload(source, payload);
            pickNext();
        }

        @Override
        public void moveFailed(){
            pickNext();
        }

        @Override
        public void updateTile(){
            super.updateTile();

            smoothRot = Mathf.slerpDelta(smoothRot, rotdeg(), 0.2f);
        }

        @Override
        public void draw(){
            Draw.rect(region, x, y);

            float dst = 0.8f;

            Draw.mixcol(team.color, Math.max((dst - (Math.abs(fract() - 0.5f) * 2)) / dst, 0));
            Draw.rect(topRegion, x, y, smoothRot);
            Draw.reset();

            Draw.rect(overRegion, x, y);

            Draw.z(Layer.blockOver);

            if(item != null){
                item.draw();
            }
        }
    }
}
