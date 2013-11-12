package com.theminequest.MQCoreRPG.util.radius;

public enum Wave 
{
    _0(new Relative[]{rel(0,0)}),
    _1(new Relative[]
        {
            rel(-1,1), rel(0,1), rel(1,1),
            rel(-1,0)          , rel(1,0),
            rel(-1,-1),rel(0,-1),rel(1, -1)
        }
      ),
    _2(new Relative[]
        {
                     rel(-1,2), rel(0,2), rel(1,2), 
            rel(-2,1),                                rel(2,1),
            rel(-2,0),                                rel(2,0),
            rel(-2,-1),                               rel(2,-1),
                     rel(-1,-2),rel(0,-2),rel(1,-2)
        }
      ),
    _3(new Relative[]
        {
                                rel(-1,3),rel(0,3),rel(1,3),
                      rel(-2,2),                            rel(2,2),
            rel(-3,1),                                               rel(3,1),
            rel(-3,0),                                               rel(3,0),
            rel(-3,-1),                                              rel(3,-1),
                      rel(-2,-2),                           rel(2,-2),
                                rel(-1,-3),rel(0,-3),rel(1,-3)
        }
      ),
    _4(new Relative[]
        {
                                  rel(-2,4),rel(-1,4),rel(0,4),rel(1,4),rel(2,4),
                       rel(-3,3), rel(-2,3),                                rel(2,3), rel(3,3),
            rel(-4,2), rel(-3,2),                                                     rel(3,2), rel(4,2),
            rel(-4,1),                                                                          rel(4,1),
            rel(-4,0),                                                                          rel(4,0),
            rel(-4,-1),                                                                         rel(4,-1),
            rel(-4,-2),rel(-3,-2),                                                    rel(3,-2),rel(4,-2),
                       rel(-3,-3),rel(-2,-3),                               rel(2,-3),rel(3,-3),
                                  rel(-2,-4),rel(-1,-4),rel(0,-4),rel(1,-4),rel(2,-4)
        }
      ),
    _5(new Relative[]
        {
                                          rel(-2,5) ,rel(-1,5) ,rel(0,5) ,rel(1,5) ,rel(2,5),
                                rel(-3,4)                                                    ,rel(3,4),
                      rel(-4,3)                                                                        ,rel(4,3),
            rel(-5,2)                                                                                            ,rel(5,2),
            rel(-5,1)                                                                                            ,rel(5,1),
            rel(-5,0)                                                                                            ,rel(5,0),
            rel(-5,-1)                                                                                           ,rel(5,-1),
            rel(-5,-2)                                                                                           ,rel(5,-2),
                      rel(-4,-3)                                                                       ,rel(4,-3),
                                rel(-3,-4)                                                   ,rel(3,-4),
                                          rel(-2,-5),rel(-1,-5),rel(0,-5),rel(1,-5),rel(2,-5),
        }
      )
    ;
    
    private final Relative[] relatives;
    
    Wave(Relative[] relatives)
    {
        this.relatives = relatives;
    }
    
    public Relative[] getRelatives()
    {
        return this.relatives;
    }
    
    public static Wave getRadius(int i)
    {
        switch(i)
        {
            case 5:
                return Wave._5;
            case 4:
                return Wave._4;
            case 3:
                return Wave._3;
            case 2:
                return Wave._2;
            case 1:
                return Wave._1;
            case 0:
                return Wave._0;
        }
        return null;
    }
    
    private static Relative rel(int dx, int dz)
    {
        return new Relative(dx, dz);
    }

}
