package com.williambl.mangojuice.api.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.util.FastColor;
import net.minecraft.util.Unit;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

@FunctionalInterface
public interface VertexProducer<Ctx> {
    Vector3fc[] CUBE_POSITIONS = {
            // TOP
            new Vector3f(0.5f, 1, 0),
            new Vector3f(0.5f, 1, 1),
            new Vector3f(-0.5f, 1, 1),
            new Vector3f(-0.5f, 1, 0),

            // BOTTOM
            new Vector3f(-0.5f, 0, 0),
            new Vector3f(-0.5f, 0, 1),
            new Vector3f(0.5f, 0, 1),
            new Vector3f(0.5f, 0, 0),

            // FRONT
            new Vector3f(-0.5f, 0, 1),
            new Vector3f(-0.5f, 1, 1),
            new Vector3f(0.5f, 1, 1),
            new Vector3f(0.5f, 0, 1),

            // BACK
            new Vector3f(0.5f, 0, 0),
            new Vector3f(0.5f, 1, 0),
            new Vector3f(-0.5f, 1, 0),
            new Vector3f(-0.5f, 0, 0),

            // LEFT
            new Vector3f(-0.5f, 0, 0),
            new Vector3f(-0.5f, 1, 0),
            new Vector3f(-0.5f, 1, 1),
            new Vector3f(-0.5f, 0, 1),

            // RIGHT
            new Vector3f(0.5f, 0, 1),
            new Vector3f(0.5f, 1, 1),
            new Vector3f(0.5f, 1, 0),
            new Vector3f(0.5f, 0, 0)
    };

    Vector3fc[] CUBE_NORMALS = {
            new Vector3f(0, 1, 0),
            new Vector3f(0, -1, 0),
            new Vector3f(0, 0, 1),
            new Vector3f(0, 0, -0),
            new Vector3f(-1, 0, 0),
            new Vector3f(1, 0, 0)
    };

    Vector3fc[] PLANE_POSITIONS = {
            new Vector3f(-0.5f, 0.0f, -0.5f),
            new Vector3f(-0.5f, 0.0f, 0.5f),
            new Vector3f(0.5f, 0.0f, 0.5f),
            new Vector3f(0.5f, 0.0f, -0.5f)
    };

    void produce(VertexConsumer consumer, PoseStack stack, Ctx ctx);

    static <Ctx> VertexProducer.PlaneBuilder<Ctx> plane() {
        return new PlaneBuilder<>();
    }

    static Consumer<PoseStack> makeContextless(BiConsumer<PoseStack, Unit> func) {
        return poseStack -> func.accept(poseStack, Unit.INSTANCE);
    }

    default BiConsumer<PoseStack, Ctx> bind(Function<Ctx, VertexConsumer> consumerFunc) {
        return (pose, ctx) -> this.produce(consumerFunc.apply(ctx), pose, ctx);
    }

    default VertexProducer<Ctx> antiRelativeTo(Function<Ctx, Camera> cameraFunc) {
        return (consumer, stack, ctx) -> {
            stack.pushPose();
            var camera = cameraFunc.apply(ctx).getPosition();
            stack.translate(-camera.x, -camera.y, -camera.z);
            this.produce(consumer, stack, ctx);
            stack.popPose();
        };
    }

    class PlaneBuilder<Ctx> {
        private @Nullable IntVertexValue<Ctx> color;
        private @Nullable FloatVertexValue<Ctx> u;
        private @Nullable FloatVertexValue<Ctx> v;
        private @Nullable IntVertexValue<Ctx> overlay;
        private @Nullable IntVertexValue<Ctx> light;
        private Direction dir = Direction.UP;
        private final List<BiConsumer<PoseStack, Ctx>> movements = new ArrayList<>();

        private sealed interface IntVertexValue<Ctx> {
            sealed interface Static<Ctx> extends IntVertexValue<Ctx> {
                record Constant<Ctx>(int value) implements Static<Ctx> {
                    @Override
                    public int get(int idx) {
                        return this.value;
                    }
                }

                record PerVertex<Ctx>(int v1, int v2, int v3, int v4) implements Static<Ctx> {
                    @Override
                    public int get(int idx) {
                        return switch (idx) {
                            case 0 -> this.v1;
                            case 1 -> this.v2;
                            case 2 -> this.v3;
                            default -> this.v4;
                        };
                    }
                }

                @Override
                default Static<Ctx> getStatic(Ctx ctx) {
                    return this;
                }

                int get(int idx);
            }
            record Dynamic<Ctx>(Function<Ctx, Static<Ctx>> sup) implements IntVertexValue<Ctx> {
                @Override
                public Static<Ctx> getStatic(Ctx ctx) {
                    return this.sup.apply(ctx);
                }
            }
            record None<Ctx>() implements IntVertexValue<Ctx> {
                @Override
                public Static<Ctx> getStatic(Ctx ctx) {
                    return null;
                }
            }
            Static<Ctx> getStatic(Ctx ctx);
        }
        private sealed interface FloatVertexValue<Ctx> {
            sealed interface Static<Ctx> extends FloatVertexValue<Ctx> {
                record Constant<Ctx>(float value) implements Static<Ctx> {
                    @Override
                    public float get(int idx) {
                        return this.value;
                    }
                }

                record PerVertex<Ctx>(float v1, float v2, float v3, float v4) implements Static<Ctx> {
                    @Override
                    public float get(int idx) {
                        return switch (idx) {
                            case 0 -> this.v1;
                            case 1 -> this.v2;
                            case 2 -> this.v3;
                            default -> this.v4;
                        };
                    }
                }
                @Override
                default Static<Ctx> getStatic(Ctx ctx) {
                    return this;
                }

                float get(int idx);
            }
            record Dynamic<Ctx>(Function<Ctx, Static<Ctx>> sup) implements FloatVertexValue<Ctx> {
                @Override
                public Static<Ctx> getStatic(Ctx ctx) {
                    return this.sup.apply(ctx);
                }
            }
            Static<Ctx> getStatic(Ctx ctx);
        }

        public PlaneBuilder<Ctx> dir(Direction dir) {
            this.dir = dir;
            return this;
        }
        public PlaneBuilder<Ctx> translate(float x, float y, float z) {
            this.movements.add((p, ctx) -> p.translate(x, y, z));
            return this;
        }
        public PlaneBuilder<Ctx> translate(double x, double y, double z) {
            this.movements.add((p, ctx) -> p.translate(x, y, z));
            return this;
        }
        public PlaneBuilder<Ctx> scale(float x, float y, float z) {
            this.movements.add((p, ctx) -> p.scale(x, y, z));
            return this;
        }
        public PlaneBuilder<Ctx> rotate(Axis axis, float radians) {
            var quat = axis.rotation(radians);
            this.movements.add((p, ctx) -> p.mulPose(quat));
            return this;
        }
        public PlaneBuilder<Ctx> rotateDegrees(Axis axis, float degrees) {
            var quat = axis.rotationDegrees(degrees);
            this.movements.add((p, ctx) -> p.mulPose(quat));
            return this;
        }
        public PlaneBuilder<Ctx> transform(BiConsumer<PoseStack, Ctx> func) {
            this.movements.add(func);
            return this;
        }
        public PlaneBuilder<Ctx> colored(float red, float green, float blue, float alpha) {
            return this.colored(FastColor.ARGB32.colorFromFloat(red, green, blue, alpha));
        }
        public PlaneBuilder<Ctx> colored(float red, float green, float blue) {
            return this.colored(red, green, blue, 1);
        }
        public PlaneBuilder<Ctx> colored(int rgba) {
            this.color = new IntVertexValue.Static.Constant<>(rgba);
            return this;
        }
        public PlaneBuilder<Ctx> colored(int rgba1, int rgba2, int rgba3, int rgba4) {
            this.color = new IntVertexValue.Static.PerVertex<>(rgba1, rgba2, rgba3, rgba4);
            return this;
        }
        public PlaneBuilder<Ctx> uv(Supplier<TextureAtlasSprite> spriteSup) {
            this.u = new FloatVertexValue.Dynamic<>(ctx -> {
                var sprite = spriteSup.get();
                return new FloatVertexValue.Static.PerVertex<>(sprite.getU0(), sprite.getU0(), sprite.getU1(), sprite.getU1());
            });
            this.v = new FloatVertexValue.Dynamic<>(ctx -> {
                var sprite = spriteSup.get();
                return new FloatVertexValue.Static.PerVertex<>(sprite.getV0(), sprite.getV1(), sprite.getV1(), sprite.getV0());
            });
            return this;
        }
        public PlaneBuilder<Ctx> uv(float minU, float maxU, float minV, float maxV) {
            this.u = new FloatVertexValue.Static.PerVertex<>(minU, minU, maxU, maxU);
            this.v = new FloatVertexValue.Static.PerVertex<>(minV, maxV, maxV, minV);
            return this;
        }
        public PlaneBuilder<Ctx> overlay(int overlay) {
            this.overlay = new IntVertexValue.Static.Constant<>(overlay);
            return this;
        }
        public PlaneBuilder<Ctx> overlay(int minU, int maxU, int minV, int maxV) {
            this.overlay = new IntVertexValue.Static.PerVertex<>(minU, maxU, minV, maxV);
            return this;
        }
        public PlaneBuilder<Ctx> light(int lightTexture) {
            this.light = new IntVertexValue.Static.Constant<>(lightTexture);
            return this;
        }
        public PlaneBuilder<Ctx> light(int minU, int maxU, int minV, int maxV) {
            this.light = new IntVertexValue.Static.PerVertex<>(minU, maxU, minV, maxV);
            return this;
        }
        //todo dynamic light/overlay/uvs
        //todo manual uv1/uv2 support
        public VertexProducer<Ctx> build() {
            return (consumer, poseStack, ctx) -> {
                poseStack.pushPose();
                @Nullable IntVertexValue.Static<Ctx> color = this.color == null ? null : this.color.getStatic(ctx);
                @Nullable var u = this.u == null ? null : this.u.getStatic(ctx);
                @Nullable var v = this.v == null ? null : this.v.getStatic(ctx);
                @Nullable var overlay = this.overlay == null ? null : this.overlay.getStatic(ctx);
                @Nullable var light = this.light == null ? null : this.light.getStatic(ctx);

                for (var movement : this.movements) {
                    movement.accept(poseStack, ctx);
                }
                poseStack.mulPose(this.dir.getRotation());
                var pose = poseStack.last();

                for (int i = 0; i < 4; i++) {
                    var vertex = PLANE_POSITIONS[i];
                    consumer.addVertex(pose, (Vector3f) vertex);
                    if (color != null) {
                        consumer.setColor(color.get(i));
                    }
                    if (u != null && v != null) {
                        consumer.setUv(u.get(i), v.get(i));
                    }
                    if (overlay != null) {
                        consumer.setOverlay(overlay.get(i));
                    }
                    if (light != null) {
                        consumer.setLight(light.get(i));
                    }
                    consumer.setNormal(pose, 0f, 1f, 0f);
                }
                poseStack.popPose();
            };
        }
    }
}
