package net.sasha.utils;

import net.sasha.packets.WrapperPlayServerWorldParticles;

import org.bukkit.Location;

import com.comphenix.protocol.wrappers.EnumWrappers.Particle;

public class ParticleUtils {
  
  public static WrapperPlayServerWorldParticles witchParticleAt(Location location) {
    WrapperPlayServerWorldParticles particle 
                                    = new WrapperPlayServerWorldParticles();
    
    particle.setParticleType(Particle.SPELL_WITCH);
    particle.setNumberOfParticles(5);
    
    particle.setX((float) location.getX());
    particle.setY((float) location.getY());
    particle.setZ((float) location.getZ());
    
    return particle;
    
  }
  
  public static WrapperPlayServerWorldParticles villagerParticleAt(Location location) {
    WrapperPlayServerWorldParticles particle 
                                    = new WrapperPlayServerWorldParticles();
    
    particle.setParticleType(Particle.EXPLOSION_HUGE);
    particle.setNumberOfParticles(20);
    
    particle.setX((float) location.getX());
    particle.setY((float) location.getY());
    particle.setZ((float) location.getZ());
    
    return particle;
    
  }

}
